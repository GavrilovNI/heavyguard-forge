package me.doggy.heavyguard.region;

import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.api.region.IRegionsProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashMap;
import java.util.Map;

public class RegionsProvider implements IRegionsProvider
{
    private final Map<ServerLevel, ServerLevelRegionsContainer> _regionsContainers = new HashMap<>();
    
    private final static RegionsProvider _instance;
    
    static
    {
        _instance = new RegionsProvider();
    }
    
    public static RegionsProvider instance()
    {
        return _instance;
    }
    
    private RegionsProvider()
    {
        MinecraftForge.EVENT_BUS.addListener(
                (ServerAboutToStartEvent event) -> _regionsContainers.clear()
        );
        MinecraftForge.EVENT_BUS.addListener(
                (ServerStoppedEvent event) -> _regionsContainers.clear()
        );

        MinecraftForge.EVENT_BUS.addListener((WorldEvent.Load event) -> loadWorldRegions(event.getWorld()));
        MinecraftForge.EVENT_BUS.addListener((WorldEvent.Save event) -> saveWorldRegions(event.getWorld()));
    }
    
    public IRegionsContainer getRegions(ServerLevel level)
    {
        return _regionsContainers.get(level);
    }
    
    private void loadWorldRegions(LevelAccessor levelAccessor)
    {
        if(levelAccessor instanceof ServerLevel level)
        {
            ServerLevelRegionsContainer regions = _regionsContainers.get(level);
            if(regions == null)
            {
                regions = new ServerLevelRegionsContainer(level);
                _regionsContainers.put(level, regions);
            }
            regions.load();
        }
    }
    
    private void saveWorldRegions(LevelAccessor levelAccessor)
    {
        if(levelAccessor instanceof ServerLevel serverLevel)
        {
            ServerLevelRegionsContainer regions = _regionsContainers.get(serverLevel);
            regions.save();
        }
    }
}
