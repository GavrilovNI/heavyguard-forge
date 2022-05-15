package me.doggy.heavyguard.command.argument.custom.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.event.region.RegionMembersEvent;
import me.doggy.heavyguard.region.RegionsProvider;
import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.command.argument.ModCommandExceptions;
import me.doggy.heavyguard.util.ServerGetter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegionNameArgumentType implements ArgumentType<String>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("MyRegion", "Spawn");
    
    public static class SuggestionsGetter
    {
        private static final String REGION_SUGGESTIONS_CHANNEL_VERSION = "1.0";
        public static final SimpleChannel REGION_SUGGESTIONS_CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(HeavyGuard.MOD_ID, "region_suggestions_channel"),
                () -> REGION_SUGGESTIONS_CHANNEL_VERSION,
                NetworkRegistry.acceptMissingOr(REGION_SUGGESTIONS_CHANNEL_VERSION),
                NetworkRegistry.acceptMissingOr(REGION_SUGGESTIONS_CHANNEL_VERSION)
        );
        
        private static boolean _serverRegistered = false;
    
        @OnlyIn(Dist.CLIENT)
        private final static Set<String> _suggestions = new HashSet<>();
        
        static
        {
            int nextMessageId = 0;
            REGION_SUGGESTIONS_CHANNEL.registerMessage(nextMessageId++, AddRegionSuggestionsMsg.class,
                    AddRegionSuggestionsMsg::encode, AddRegionSuggestionsMsg::new, AddRegionSuggestionsMsg::handle);
            REGION_SUGGESTIONS_CHANNEL.registerMessage(nextMessageId++, RemoveRegionSuggestionsMsg.class,
                    RemoveRegionSuggestionsMsg::encode, RemoveRegionSuggestionsMsg::new, RemoveRegionSuggestionsMsg::handle);
            REGION_SUGGESTIONS_CHANNEL.registerMessage(nextMessageId++, ClearRegionSuggestionsMsg.class,
                    ClearRegionSuggestionsMsg::encode, ClearRegionSuggestionsMsg::new, ClearRegionSuggestionsMsg::handle);
        }
    
        public static class AddRegionSuggestionsMsg
        {
            private final Set<String> _suggestionsToAdd;
            
            public AddRegionSuggestionsMsg(FriendlyByteBuf buf)
            {
                _suggestionsToAdd = new HashSet<>();
                int size = buf.readInt();
                for(int i = 0; i < size; i++)
                    _suggestionsToAdd.add(buf.readUtf());
            }
            public AddRegionSuggestionsMsg(String suggestion)
            {
                _suggestionsToAdd = new HashSet<>();
                _suggestionsToAdd.add(suggestion);
            }
            public AddRegionSuggestionsMsg(Collection<String> newSuggestions)
            {
                _suggestionsToAdd = newSuggestions.stream().collect(Collectors.toSet());
            }
        
            public void encode(FriendlyByteBuf buf)
            {
                buf.writeInt(_suggestionsToAdd.size());
                for(var suggestion : _suggestionsToAdd)
                    buf.writeUtf(suggestion);
            }
        
            public void handle(Supplier<NetworkEvent.Context> contextSupplier)
            {
                contextSupplier.get().enqueueWork(() -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                        _suggestions.addAll(_suggestionsToAdd);
                    });
                });
                contextSupplier.get().setPacketHandled(true);
            }
        }
        public static class RemoveRegionSuggestionsMsg
        {
            private final Set<String> _suggestionsToRemove;
            
            public RemoveRegionSuggestionsMsg(FriendlyByteBuf buf)
            {
                _suggestionsToRemove = new HashSet<>();
                int size = buf.readInt();
                for(int i = 0; i < size; i++)
                    _suggestionsToRemove.add(buf.readUtf());
            }
            public RemoveRegionSuggestionsMsg(String suggestion)
            {
                _suggestionsToRemove = new HashSet<>();
                _suggestionsToRemove.add(suggestion);
            }
            public RemoveRegionSuggestionsMsg(Collection<String> suggestionsToRemove)
            {
                _suggestionsToRemove = suggestionsToRemove.stream().collect(Collectors.toSet());
            }
        
            public void encode(FriendlyByteBuf buf)
            {
                buf.writeInt(_suggestionsToRemove.size());
                for(var suggestion : _suggestionsToRemove)
                    buf.writeUtf(suggestion);
            }
        
            public void handle(Supplier<NetworkEvent.Context> contextSupplier)
            {
                contextSupplier.get().enqueueWork(() -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                        _suggestions.removeAll(_suggestionsToRemove);
                    });
                });
                contextSupplier.get().setPacketHandled(true);
            }
        }
        public static class ClearRegionSuggestionsMsg
        {
            public ClearRegionSuggestionsMsg(FriendlyByteBuf buf)
            {
            
            }
            public ClearRegionSuggestionsMsg()
            {
        
            }
        
            public void encode(FriendlyByteBuf buf)
            {
            
            }
        
            public void handle(Supplier<NetworkEvent.Context> contextSupplier)
            {
                contextSupplier.get().enqueueWork(() -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                        _suggestions.clear();
                    });
                });
                contextSupplier.get().setPacketHandled(true);
            }
        }
        
        public static Set<String> getDefaultSuggestions(ServerPlayer player)
        {
            var server = ServerGetter.getServer();
            Set<String> result = new HashSet<>();
            for(var level : server.getAllLevels())
            {
                IRegionsContainer regionsContainer = RegionsProvider.instance().getRegions(level);
                var regionsToAdd = regionsContainer.getRegions(
                        r -> r.getMembers().getPlayerMembership(player.getUUID()) != Membership.Stranger).map(
                        r -> r.getName()).collect(Collectors.toSet());
                result.addAll(regionsToAdd);
            }
            return result;
        }
        
        public static void sendAddSuggestions(ServerPlayer player, Collection<String> suggestionsToAdd)
        {
            REGION_SUGGESTIONS_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AddRegionSuggestionsMsg(suggestionsToAdd));
        }
        public static void sendRemoveSuggestions(ServerPlayer player, Collection<String> suggestionsToRemove)
        {
            REGION_SUGGESTIONS_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RemoveRegionSuggestionsMsg(suggestionsToRemove));
        }
        public static void sendAddSuggestions(ServerPlayer player, AddRegionSuggestionsMsg msg)
        {
            REGION_SUGGESTIONS_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
        public static void sendRemoveSuggestions(ServerPlayer player, RemoveRegionSuggestionsMsg msg)
        {
            REGION_SUGGESTIONS_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
        public static void sendClearSuggestions(ServerPlayer player)
        {
            REGION_SUGGESTIONS_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClearRegionSuggestionsMsg());
        }
        public static void registerOnClient()
        {
            // registering channel in static constructor
        }
        public static void registerOnServer()
        {
            if(_serverRegistered)
                return;
            _serverRegistered = true;
            
            MinecraftForge.EVENT_BUS.addListener(
                    (PlayerEvent.PlayerLoggedInEvent event) -> {
                        if(event.getPlayer() instanceof ServerPlayer player)
                            sendAddSuggestions(player, getDefaultSuggestions(player));
                    });
    
            HeavyGuard.getEventBus().addListener(
                    (RegionMembersEvent.MembershipUpdated event) -> {
                        var region = event.getRegion();
                        var newMembership = event.getNewMembership();
                        var playerUUID = event.getMemberUuid();
                        var player = ServerGetter.getServer().getPlayerList().getPlayer(playerUUID);
                        
                        if(player == null)
                            return;
                        if(newMembership == Membership.Stranger)
                            sendRemoveSuggestions(player, List.of(region.getName()));
                        else
                            sendAddSuggestions(player, List.of(region.getName()));
                    });
    
            HeavyGuard.getEventBus().addListener(
                    (RegionEvent.Removing event) -> {
                        var region = event.getRegion();
                        var msg = new RemoveRegionSuggestionsMsg(region.getName());
                        var serverPlayers = ServerGetter.getServer().getPlayerList();
                        for(var playerEntry : region.getMembers())
                        {
                            var player = serverPlayers.getPlayer(playerEntry.getKey());
                            if(player != null)
                                sendRemoveSuggestions(player, msg);
                        }
                    });
            HeavyGuard.getEventBus().addListener(
                    (RegionEvent.Added event) -> {
                        var region = event.getRegion();
                        var msg = new AddRegionSuggestionsMsg(region.getName());
                        var serverPlayers = ServerGetter.getServer().getPlayerList();
                        for(var playerEntry : region.getMembers())
                        {
                            var player = serverPlayers.getPlayer(playerEntry.getKey());
                            if(player != null)
                                sendAddSuggestions(player, msg);
                        }
                    });
        }
    }
    
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException
    {
        String regionName = reader.readUnquotedString();
        if(regionName.isEmpty())
            throw ModCommandExceptions.MISSED_ARGUMENT_EXCEPTION.create(reader.getCursor());
        return regionName;
    }
    
    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        if(context.getSource() instanceof SharedSuggestionProvider)
        {
            return SharedSuggestionProvider.suggest(SuggestionsGetter._suggestions, builder);
        }
        else
        {
            return Suggestions.empty();
        }
    }
    
    public static RegionNameArgumentType name()
    {
        return new RegionNameArgumentType();
    }
    
    public static String getName(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        return context.getArgument(name, String.class);
    }
    
    public static void register()
    {
        ArgumentTypes.register(new ResourceLocation(HeavyGuard.MOD_ID, "region_name").toString(), RegionNameArgumentType.class,
                new EmptyArgumentSerializer<>(RegionNameArgumentType::name));
    }
}
