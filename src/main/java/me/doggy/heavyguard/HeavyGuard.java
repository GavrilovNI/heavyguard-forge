package me.doggy.heavyguard;

import com.mojang.logging.LogUtils;
import me.doggy.heavyguard.command.argument.custom.type.RegionNameArgumentType;
import me.doggy.heavyguard.item.ModItems;
import me.doggy.heavyguard.region.RegionsProvider;
import me.doggy.heavyguard.region.client.ClientRegions;
import me.doggy.heavyguard.region.client.ClientRegionsUpdater;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
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
    
    private void setupCommon(final FMLCommonSetupEvent event)
    {
        ClientRegionsUpdater.registerOnServer();
        RegionNameArgumentType.SuggestionsGetter.registerOnServer();
        RegionsProvider.instance(); // registering listeners
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
    
    public static IEventBus getEventBus()
    {
        return EVENT_BUS;
    }
}
