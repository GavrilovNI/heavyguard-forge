package me.doggy.heavyguard;

import com.mojang.logging.LogUtils;
import me.doggy.heavyguard.api.region.IRegionsProvider;
import me.doggy.heavyguard.command.argument.custom.type.RegionNameArgumentType;
import me.doggy.heavyguard.api.interaction.IInteractionHandler;
import me.doggy.heavyguard.interaction.InteractionHandler;
import me.doggy.heavyguard.item.ModItems;
import me.doggy.heavyguard.region.RegionsProvider;
import me.doggy.heavyguard.region.client.ClientRegions;
import me.doggy.heavyguard.region.client.ClientRegionsUpdater;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HeavyGuard.MOD_ID)
public class HeavyGuard
{
    public static final String MOD_ID = "heavyguard";
    public static final String MOD_NAME = "Heavy Guard";
    
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus EVENT_BUS = BusBuilder.builder().build();
    
    private static final RegionsProvider _regionsProvider = new RegionsProvider();
    private static final InteractionHandler _interactionHandler = new InteractionHandler(_regionsProvider);
    
    private static final DefaultInteractionEventsSubscriber _defaultSubscriber = new DefaultInteractionEventsSubscriber(_interactionHandler);
    
    public HeavyGuard()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    
        ModItems.register(eventBus);
        
        eventBus.addListener(this::setupCommon);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> eventBus.addListener(this::setupClient));
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER,
                () -> () -> eventBus.addListener(this::setupDedicatedServer));
        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public static IRegionsProvider getRegionsProvider()
    {
        return _regionsProvider;
    }
    public static IInteractionHandler getInteractionHandler()
    {
        return _interactionHandler;
    }
    
    private void setupCommon(final FMLCommonSetupEvent event)
    {
        ClientRegionsUpdater.registerOnServer();
        RegionNameArgumentType.SuggestionsGetter.registerOnServer();
    
        
    }
    private void setupClient(final FMLCommonSetupEvent event)
    {
        setupCommon(event);
        ClientRegions.instance().registerOnClient();
        RegionNameArgumentType.SuggestionsGetter.registerOnClient();
    
        LOGGER.info("Mod " + MOD_NAME + " has started on client!");
    }
    private void setupDedicatedServer(final FMLCommonSetupEvent event)
    {
        setupCommon(event);
        LOGGER.info("Mod " + MOD_NAME + " has started on server!");
    }
    
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event)
    {
        _regionsProvider.clear();
    }
    
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event)
    {
        _regionsProvider.clear();
    }
    
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        _regionsProvider.loadWorldRegions(event.getWorld());
    }
    
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Save event)
    {
        _regionsProvider.saveWorldRegions(event.getWorld());
    }
    
    public static IEventBus getEventBus()
    {
        return EVENT_BUS;
    }
}
