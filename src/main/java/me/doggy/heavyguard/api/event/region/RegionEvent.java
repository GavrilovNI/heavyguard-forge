package me.doggy.heavyguard.api.event.region;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.region.RegionsProvider;
import net.minecraftforge.eventbus.api.Event;

public abstract class RegionEvent extends Event
{
    public static void postEventBy(IRegionsContainer regions, Event event)
    {
        regions.getEventBus().post(event);
        if(RegionsProvider.instance().getRegions(regions.getLevel()) == regions)
            HeavyGuard.getEventBus().post(event);
    }
    public static void postEventBy(IRegion region, Event event)
    {
        region.getEventBus().post(event);
        
        var level = region.getLevel();
        var regions = RegionsProvider.instance().getRegions(level);
        if(regions.hasRegion(region))
        {
            regions.getEventBus().post(event);
            HeavyGuard.getEventBus().post(event);
        }
    }
    
    private final IRegion _region;
    
    public RegionEvent(IRegion region)
    {
        _region = region;
    }
    
    public IRegion getRegion()
    {
        return _region;
    }
    
    public static class Loaded extends RegionEvent
    {
        public Loaded(IRegion region)
        {
            super(region);
        }
    }
    
    public static class Saved extends RegionEvent
    {
        public Saved(IRegion region)
        {
            super(region);
        }
    }
    
    public static class Added extends RegionEvent
    {
        public Added(IRegion region)
        {
            super(region);
        }
    }
    
    public static class Removing extends RegionEvent
    {
        public Removing(IRegion region)
        {
            super(region);
        }
    }
}
