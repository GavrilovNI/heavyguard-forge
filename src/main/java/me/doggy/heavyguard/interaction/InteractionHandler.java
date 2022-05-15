package me.doggy.heavyguard.interaction;

import me.doggy.heavyguard.api.interaction.IInteractedByEntity;
import me.doggy.heavyguard.api.interaction.IInteractionHandler;
import me.doggy.heavyguard.api.interaction.Interaction;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.api.region.IRegionsProvider;
import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.api.utils.TextBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Collection;
import java.util.Objects;

public class InteractionHandler implements IInteractionHandler
{
    private final IEventBus _eventBus = BusBuilder.builder().build();
    
    private final IRegionsProvider _regionsProvider;
    
    public InteractionHandler(IRegionsProvider regionsProvider)
    {
        _regionsProvider = regionsProvider;
        _eventBus.addListener(this::defaultInteractionTest);
    }
    
    public IEventBus getEventBus()
    {
        return _eventBus;
    }
    
    private boolean canInteract(FlagTypePath path, Collection<IRegion> regions)
    {
        for(var region : regions)
        {
            if(region.canInteract(path) == false)
                return false;
        }
        return true;
    }
    
    private boolean canInteract(FlagTypePath path, IRegionsContainer regions, Vec3 position)
    {
        return canInteract(path, regions.getRegions(position));
    }
    
    private void defaultInteractionTest(Interaction interaction)
    {
        if(interaction.isCanceled())
            return;
        
        var location = interaction.getLocation();
        var path = interaction.getFlagPath();
    
        var regions = _regionsProvider.getRegions(location.getLevel());
    
        var canInteract = canInteract(path, regions, location.getPosition());
        interaction.setCanceled(canInteract == false);
    }
    
    private void sendNoAccessMessage(Interaction interaction)
    {
        if(interaction instanceof IInteractedByEntity interactedByEntity)
        {
            if(interactedByEntity.getInteractor() instanceof Player player)
            {
                TextBuilder.of("You don't have region permission '" + interaction.getFlagPath() + "' to do that!", ChatFormatting.RED)
                        .send(player, ChatType.GAME_INFO);
            }
        }
    }
    
    public boolean test(Interaction interaction)
    {
        Objects.requireNonNull(interaction);
        boolean cancelled = _eventBus.post(interaction);
        if(cancelled)
            sendNoAccessMessage(interaction);
        return cancelled;
    }
}
