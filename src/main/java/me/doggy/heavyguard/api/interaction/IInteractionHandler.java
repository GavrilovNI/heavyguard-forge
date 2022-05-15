package me.doggy.heavyguard.api.interaction;

import net.minecraftforge.eventbus.api.IEventBus;

public interface IInteractionHandler
{
    boolean test(Interaction interaction);
    
    IEventBus getEventBus();
}
