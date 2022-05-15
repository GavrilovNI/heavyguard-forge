package me.doggy.heavyguard.region.client;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.event.region.BoundedRegionEvent;
import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.event.region.RegionMembersEvent;
import me.doggy.heavyguard.region.RegionsProvider;
import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IBoundedRegion;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.util.FriendlyByteBufHelper;
import me.doggy.heavyguard.util.LevelUtils;
import me.doggy.heavyguard.util.ServerGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClientRegionsUpdater
{
    public static final int maxVisibilityRadius = 64;
    public static final int minDistanceToUpdate = 16;
    private static final int minDistanceToUpdateSqr = minDistanceToUpdate * minDistanceToUpdate;
    
    private static final String CLIENT_REGION_CHANNEL_VERSION = "1.0";
    public static final SimpleChannel CLIENT_REGION_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HeavyGuard.MOD_ID, "client_regions"),
            () -> CLIENT_REGION_CHANNEL_VERSION,
            NetworkRegistry.acceptMissingOr(CLIENT_REGION_CHANNEL_VERSION),
            NetworkRegistry.acceptMissingOr(CLIENT_REGION_CHANNEL_VERSION)
    );
    
    private static boolean _serverRegistered = false;
    
    static
    {
        int nextMessageId = 0;
        CLIENT_REGION_CHANNEL.registerMessage(nextMessageId++, UpdateClientRegionsMsg.class,
                UpdateClientRegionsMsg::encode, UpdateClientRegionsMsg::new, UpdateClientRegionsMsg::handle);
        CLIENT_REGION_CHANNEL.registerMessage(nextMessageId++, AddClientRegionMsg.class,
                AddClientRegionMsg::encode, AddClientRegionMsg::new, AddClientRegionMsg::handle);
        CLIENT_REGION_CHANNEL.registerMessage(nextMessageId++, RemoveClientRegionMsg.class,
                RemoveClientRegionMsg::encode, RemoveClientRegionMsg::new, RemoveClientRegionMsg::handle);
    }
    
    public static class UpdateClientRegionsMsg implements Iterable<ClientBoundedRegion>
    {
        private final Set<ClientBoundedRegion> _regions;
        
        public UpdateClientRegionsMsg(FriendlyByteBuf buf)
        {
            _regions = new HashSet<>();
            int size = buf.readInt();
            for(int i = 0; i < size; i++)
                _regions.add(getRegionFromBuf(buf));
        }
        public UpdateClientRegionsMsg(Collection<ClientBoundedRegion> regions)
        {
            _regions = regions.stream().collect(Collectors.toSet());
        }
        
        public void encode(FriendlyByteBuf buf)
        {
            buf.writeInt(_regions.size());
            for(var region : _regions)
                addRegionToBuf(buf, region);
        }
        
        public void handle(Supplier<NetworkEvent.Context> contextSupplier)
        {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    ClientRegions.updateRegions(this, contextSupplier);
                });
            });
            contextSupplier.get().setPacketHandled(true);
        }
    
        @NotNull
        @Override
        public Iterator<ClientBoundedRegion> iterator()
        {
            return _regions.iterator();
        }
    }
    public static class AddClientRegionMsg
    {
        private final ClientBoundedRegion _region;
        
        public AddClientRegionMsg(FriendlyByteBuf buf)
        {
            _region =getRegionFromBuf(buf);
        }
        public AddClientRegionMsg(ClientBoundedRegion region)
        {
            _region = region;
        }
        
        public ClientBoundedRegion getRegion()
        {
            return _region;
        }
        
        public void encode(FriendlyByteBuf buf)
        {
            addRegionToBuf(buf, _region);
        }
        
        public void handle(Supplier<NetworkEvent.Context> contextSupplier)
        {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    ClientRegions.addRegion(this, contextSupplier);
                });
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
    public static class RemoveClientRegionMsg
    {
        private final ResourceLocation _levelIdentifier;
        private final String _regionName;
        
        public RemoveClientRegionMsg(FriendlyByteBuf buf)
        {
            _levelIdentifier = buf.readResourceLocation();
            _regionName = buf.readUtf();
        }
        public RemoveClientRegionMsg(ResourceLocation worldIdentifier, String regionName)
        {
            _levelIdentifier = worldIdentifier;
            _regionName = regionName;
        }
    
        public ResourceLocation getLevelIdentifier()
        {
            return _levelIdentifier;
        }
        public String getRegionName()
        {
            return _regionName;
        }
        
        public void encode(FriendlyByteBuf buf)
        {
            buf.writeResourceLocation(_levelIdentifier);
            buf.writeUtf(_regionName);
        }
        
        public void handle(Supplier<NetworkEvent.Context> contextSupplier)
        {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    ClientRegions.removeRegion(this, contextSupplier);
                });
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
    
    private static final Map<Player, Vec3> _lastPlayersPosition = new HashMap<>();
    
    public static BoundsInt getPlayerVisibilityBounds(Vec3i playerPosition, int radius)
    {
        Vec3i radiusVector = new Vec3i(1, 1, 1).multiply(radius);
        return new BoundsInt(playerPosition.subtract(radiusVector), playerPosition.offset(radiusVector));
    }
    public static boolean canPlayerSee(Vec3i playerPosition, BoundsInt visibilityBounds, BoundsInt regionBounds)
    {
        var closestPosition = regionBounds.getClosestPosition(Vec3.atLowerCornerOf(playerPosition));
        return visibilityBounds.contains(closestPosition);
    }
    public static boolean canPlayerSee(Vec3i playerPosition, int radius, BoundsInt regionBounds)
    {
        var visibilityBounds = getPlayerVisibilityBounds(playerPosition, radius);
        return canPlayerSee(playerPosition, visibilityBounds, regionBounds);
    }
    
    private static ClientBoundedRegion getRegionFromBuf(FriendlyByteBuf buf)
    {
        String name = buf.readUtf();
        BoundsInt bounds = FriendlyByteBufHelper.readBoundsInt(buf);
        ResourceLocation worldIdentifier = ResourceLocation.tryParse(buf.readUtf());
        Membership membership = FriendlyByteBufHelper.readEnum(buf, Membership.class);
        return new ClientBoundedRegion(name, bounds, worldIdentifier, membership);
    }
    
    private static void addRegionToBuf(FriendlyByteBuf buf, ClientBoundedRegion region)
    {
        buf.writeUtf(region.getName());
        FriendlyByteBufHelper.writeBoundsInt(buf, region.getBounds());
        buf.writeUtf(region.getWorldIdentifier().toString());
        FriendlyByteBufHelper.writeEnum(buf, region.getMembership());
    }
    
    private static void updateRegions(ServerPlayer player, Collection<ClientBoundedRegion> regions)
    {
        CLIENT_REGION_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateClientRegionsMsg(regions));
        _lastPlayersPosition.put(player, player.position());
    }
    private static void updateRegions(ServerPlayer player, int radius)
    {
        Vec3i playerPosition = player.blockPosition();
        BoundsInt bounds = getPlayerVisibilityBounds(playerPosition, radius);
        
        ServerLevel level = player.getLevel();
        IRegionsContainer regionsContainer = RegionsProvider.instance().getRegions(level);
        
        var regions = regionsContainer.getBoundedRegions(bounds);
        
        updateRegions(player, regions.stream().map(r -> new ClientBoundedRegion(r, player)).collect(Collectors.toSet()));
    }
    private static void addRegion(ServerPlayer player, ClientBoundedRegion region)
    {
        CLIENT_REGION_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AddClientRegionMsg(region));
    }
    private static void removeRegion(ServerPlayer player, ClientBoundedRegion region)
    {
        removeRegion(player, region.getWorldIdentifier(), region.getName());
    }
    private static void removeRegion(ServerPlayer player, ResourceLocation worldIdentifier, String regionName)
    {
        CLIENT_REGION_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RemoveClientRegionMsg(worldIdentifier, regionName));
    }
    
    private static void addRegion(IBoundedRegion region)
    {
        var players = ServerGetter.getServer().getPlayerList().getPlayers();
        for(var player : players)
            if(canPlayerSee(player.blockPosition(), maxVisibilityRadius, region.getBounds()))
                addRegion(player, new ClientBoundedRegion(region, player));
    }
    
    private static void onRegionAdded(RegionEvent.Added event)
    {
        if(event.getRegion() instanceof IBoundedRegion boundedRegion)
            addRegion(boundedRegion);
    }
    
    private static void onRegionBoundsUpdated(BoundedRegionEvent.BoundsUpdated event)
    {
        addRegion(event.getRegion());
    }
    
    private static void onRegionMemberMembershipUpdated(RegionMembersEvent.MembershipUpdated event)
    {
        if(event.getRegion() instanceof IBoundedRegion boundedRegion)
        {
            var player = ServerGetter.getServer().getPlayerList().getPlayer(event.getMemberUuid());
            if(player != null)
                if(canPlayerSee(player.blockPosition(), maxVisibilityRadius, boundedRegion.getBounds()))
                    addRegion(player, new ClientBoundedRegion(boundedRegion, player));
        }
    }
    
    private static void onRegionRemoving(RegionEvent.Removing event)
    {
        if(event.getRegion() instanceof IBoundedRegion boundedRegion)
        {
            var players = ServerGetter.getServer().getPlayerList().getPlayers();
            for(var player : players)
                removeRegion(player, LevelUtils.getIdentifier(boundedRegion.getLevel()), boundedRegion.getName());
        }
    }
    
    public static void registerNetworkChannel()
    {
        // registers in static constructor
    }
    
    public static void registerOnServer()
    {
        if(_serverRegistered)
            return;
        _serverRegistered = true;
        
        MinecraftForge.EVENT_BUS.addListener(
                (EntityJoinWorldEvent event) -> {
                    if(event.getEntity() instanceof ServerPlayer player)
                        updateRegions(player, maxVisibilityRadius);
                });
    
        MinecraftForge.EVENT_BUS.addListener(
                (EntityMoveEvent.Moved event) -> {
                    if(event.getEntity() instanceof ServerPlayer player)
                    {
                        var lastPosition = _lastPlayersPosition.get(player);
                        var oldLocation = event.getOldLocation();
                        var newLocation = event.getNewLocation();
                        if(oldLocation.getLevel() != newLocation.getLevel()
                                || lastPosition.distanceToSqr(newLocation.getPosition()) >= minDistanceToUpdateSqr)
                            updateRegions(player, maxVisibilityRadius);
                    }
                });
        
        HeavyGuard.getEventBus().addListener(ClientRegionsUpdater::onRegionAdded);
        HeavyGuard.getEventBus().addListener(ClientRegionsUpdater::onRegionBoundsUpdated);
        HeavyGuard.getEventBus().addListener(ClientRegionsUpdater::onRegionMemberMembershipUpdated);
        HeavyGuard.getEventBus().addListener(ClientRegionsUpdater::onRegionRemoving);
    }
}
