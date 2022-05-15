package me.doggy.heavyguard.api.event.region;

import me.doggy.heavyguard.api.region.IRegionsContainer;
import net.minecraftforge.eventbus.api.Event;

public abstract class RegionsEvent extends Event
{
    private final IRegionsContainer _regionsContainer;
    
    public RegionsEvent(IRegionsContainer regionsContainer)
    {
        _regionsContainer = regionsContainer;
    }
    
    public static class Saved extends RegionsEvent
    {
        public Saved(IRegionsContainer regionsContainer)
        {
            super(regionsContainer);
        }
    }
    
    public static class Loaded extends RegionsEvent
    {
        public Loaded(IRegionsContainer regionsContainer)
        {
            super(regionsContainer);
        }
    }
}
