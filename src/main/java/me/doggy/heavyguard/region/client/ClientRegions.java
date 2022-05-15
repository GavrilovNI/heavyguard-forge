package me.doggy.heavyguard.region.client;

import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.util.LevelUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientRegions implements Iterable<ClientBoundedRegion>
{
    public static final int minDistanceToUpdate = 16;
    public static final int maxVisibilityRadius = ClientRegionsUpdater.maxVisibilityRadius + ClientRegionsUpdater.minDistanceToUpdate + 1;
    
    private static final ClientRegions _instance = new ClientRegions();
    public static ClientRegions instance()
    {
        return _instance;
    }
    
    private final Map<String, ClientBoundedRegion> _regions = new HashMap<>();
    private Vec3 oldPlayerPos;
    
    private ClientRegions()
    {
    
    }
    
    private static boolean isRegionFit(ResourceLocation regionLevelIdentifier)
    {
        var level = Minecraft.getInstance().level;
        return level == null || LevelUtils.getIdentifier(level).equals(regionLevelIdentifier);
    }
    private static boolean isRegionFit(ClientBoundedRegion region)
    {
        return isRegionFit(region.getWorldIdentifier());
    }
    
    public static void updateRegions(ClientRegionsUpdater.UpdateClientRegionsMsg msg, Supplier<NetworkEvent.Context> contextSupplier)
    {
        for(var region : msg)
            if(isRegionFit(region))
                _instance._regions.put(region.getName(), region);
    }
    
    public static void addRegion(ClientRegionsUpdater.AddClientRegionMsg msg, Supplier<NetworkEvent.Context> contextSupplier)
    {
        var region = msg.getRegion();
        if(isRegionFit(region))
            _instance._regions.put(region.getName(), region);
    }
    
    public static void removeRegion(ClientRegionsUpdater.RemoveClientRegionMsg msg, Supplier<NetworkEvent.Context> contextSupplier)
    {
        ResourceLocation levelIdentifier = msg.getLevelIdentifier();
        String regionName = msg.getRegionName();
        if(isRegionFit(levelIdentifier))
            _instance._regions.remove(regionName);
    }
    
    private void removeWrongWorldRegions(Level newLevel)
    {
        var worldIdentifier = LevelUtils.getIdentifier(newLevel);
        var iterator = _regions.entrySet().iterator();
        while (iterator.hasNext()) {
            var region = iterator.next();
            if(worldIdentifier.equals(region.getValue().getWorldIdentifier()) == false)
                iterator.remove();
        }
    }
    private void removeFarRegions(Vec3i playerPosition, int radius)
    {
        var visibilityBounds = ClientRegionsUpdater.getPlayerVisibilityBounds(playerPosition, radius);
        
        var iterator = _regions.entrySet().iterator();
        while (iterator.hasNext()) {
            var region = iterator.next();
            if(ClientRegionsUpdater.canPlayerSee(playerPosition, visibilityBounds, region.getValue().getBounds()) == false)
                iterator.remove();
        }
    }
    
    public void registerOnClient()
    {
        ClientRegionsUpdater.registerNetworkChannel();
        
        MinecraftForge.EVENT_BUS.addListener(
                (EntityJoinWorldEvent event) -> {
                    if(event.getEntity() instanceof LocalPlayer player)
                    {
                        removeWrongWorldRegions(player.level);
                        oldPlayerPos = player.position();
                    }
                });
    
        MinecraftForge.EVENT_BUS.addListener(
                (EntityMoveEvent.Moved event) -> {
                    if(event.getEntity() instanceof LocalPlayer player)
                    {
                        var oldLocation = event.getOldLocation();
                        var newLocation = event.getNewLocation();
                        if(oldLocation.getLevel() == newLocation.getLevel()
                                && oldPlayerPos.distanceTo(newLocation.getPosition()) >= minDistanceToUpdate)
                        {
                            removeFarRegions(player.blockPosition(), maxVisibilityRadius);
                            oldPlayerPos = Minecraft.getInstance().player.position();
                        }
                    }
                });
        
    }
    
    @NotNull
    @Override
    public Iterator<ClientBoundedRegion> iterator()
    {
        return _regions.values().iterator();
    }
}
