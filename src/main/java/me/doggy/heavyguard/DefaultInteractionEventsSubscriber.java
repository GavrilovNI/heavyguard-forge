package me.doggy.heavyguard;

import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.interaction.EntityEnterAndLeaveInteraction;
import me.doggy.heavyguard.api.interaction.IInteractionHandler;
import me.doggy.heavyguard.api.interaction.Interaction;
import me.doggy.heavyguard.api.interaction.block.BlockInteractions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public class DefaultInteractionEventsSubscriber
{
    private final IInteractionHandler _interactionHandler;
    
    public DefaultInteractionEventsSubscriber(IInteractionHandler interactionHandler)
    {
        _interactionHandler = interactionHandler;
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void test(@Nullable Interaction interaction, Event event)
    {
        if(interaction != null)
            if(_interactionHandler.test(interaction).isCancelled())
                event.setCanceled(true);
    }
    
    @SubscribeEvent
    public void OnPlayerAttackBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        var interaction = BlockInteractions.EntityAttackBlock.create(event);
        test(interaction, event);
    }
    
    @SubscribeEvent
    public void OnPlayerBreakBlock(BlockEvent.BreakEvent event)
    {
        var interaction = BlockInteractions.EntityBreakBlock.create(event);
        test(interaction, event);
    }
    
    @SubscribeEvent
    public void OnEntityPlaceBlock(BlockEvent.EntityPlaceEvent event)
    {
        var interaction = BlockInteractions.EntityPlaceBlock.create(event);
        test(interaction, event);
    }
    
    @SubscribeEvent
    public void OnPlayerUseBlock(PlayerInteractEvent.RightClickBlock event)
    {
        var interaction = BlockInteractions.EntityUseBlock.create(event);
        test(interaction, event);
    }
    
    @SubscribeEvent
    public void OnEntityMove(EntityMoveEvent event)
    {
        var interaction = EntityEnterAndLeaveInteraction.create(event);
        test(interaction, event);
    }
}
