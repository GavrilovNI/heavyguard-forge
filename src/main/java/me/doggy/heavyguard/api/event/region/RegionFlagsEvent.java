package me.doggy.heavyguard.api.event.region;

import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.flag.FlagPath;

public abstract class RegionFlagsEvent extends RegionEvent
{
    private final FlagPath _path;
    
    public RegionFlagsEvent(IRegion region, FlagPath path)
    {
        super(region);
        _path = path;
    }
    
    public FlagPath getPath()
    {
        return _path;
    }
    
    public static class FlagUpdated extends RegionFlagsEvent
    {
        private final Boolean _value;
        
        public FlagUpdated(IRegion region, FlagPath path, Boolean value)
        {
            super(region, path);
            _value = value;
        }
    }
    
    public static class FlagRemoved extends RegionFlagsEvent
    {
        public FlagRemoved(IRegion region, FlagPath path)
        {
            super(region, path);
        }
    }
}
