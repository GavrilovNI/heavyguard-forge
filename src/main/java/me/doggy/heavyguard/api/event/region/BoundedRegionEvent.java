package me.doggy.heavyguard.api.event.region;

import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IBoundedRegion;
import net.minecraftforge.eventbus.api.Event;

public abstract class BoundedRegionEvent extends Event
{
    private final IBoundedRegion _region;
    
    public BoundedRegionEvent(IBoundedRegion region)
    {
        _region = region;
    }
    
    public IBoundedRegion getRegion()
    {
        return _region;
    }
    
    public static class BoundsUpdated extends BoundedRegionEvent
    {
        private final BoundsInt _oldBounds;
        
        public BoundsUpdated(IBoundedRegion region, BoundsInt oldBounds)
        {
            super(region);
            _oldBounds = oldBounds;
        }
        
        public BoundsInt getOldBounds()
        {
            return _oldBounds;
        }
    }
}
