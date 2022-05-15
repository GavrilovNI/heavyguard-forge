package me.doggy.heavyguard.region;

import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.region.IRegion;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Function;

public class RegionPart extends Pollutable
{
    private IRegion _region = null;
    
    protected void setRegion(IRegion region)
    {
        if(_region == null)
            _region = region;
        else
            throw new IllegalStateException("region already set");
    }
    
    protected IRegion getRegion()
    {
        return _region;
    }
    
    protected void postEventByRegion(Function<IRegion, Event> eventSupplier)
    {
        if(_region != null)
            RegionEvent.postEventBy(_region, eventSupplier.apply(_region));
    }
}
