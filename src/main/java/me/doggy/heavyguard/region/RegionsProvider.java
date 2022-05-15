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
    
    public IRegionsContainer getRegions(ServerLevel level)
    {
        return _regionsContainers.get(level);
    }
    
    public void clear()
    {
        _regionsContainers.clear();
    }
    
    public void loadWorldRegions(LevelAccessor levelAccessor)
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
    
    public void saveWorldRegions(LevelAccessor levelAccessor)
    {
        if(levelAccessor instanceof ServerLevel serverLevel)
        {
            ServerLevelRegionsContainer regions = _regionsContainers.get(serverLevel);
            regions.save();
        }
    }
}
