package me.doggy.heavyguard;

import me.doggy.heavyguard.api.interaction.IInteractionHandler;
import me.doggy.heavyguard.api.interaction.block.BlockInteractions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DefaultInteractionEventsSubscriber
{
    private final IInteractionHandler _interactionHandler;
    
    public DefaultInteractionEventsSubscriber(IInteractionHandler interactionHandler)
    {
        _interactionHandler = interactionHandler;
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    
    @SubscribeEvent
    public void OnPlayerBreakBlock(BlockEvent.BreakEvent event)
    {
        var interaction = BlockInteractions.EntityBreakBlock.create(event);
        if(interaction != null)
            if(_interactionHandler.test(interaction))
                event.setCanceled(true);
    }
    
    @SubscribeEvent
    public void OnEntityPlaceBlock(BlockEvent.EntityPlaceEvent event)
    {
        var interaction = BlockInteractions.EntityPlaceBlock.create(event);
        if(interaction != null)
            if(_interactionHandler.test(interaction))
                event.setCanceled(true);
    }
}
