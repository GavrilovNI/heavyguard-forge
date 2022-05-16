package me.doggy.heavyguard.interaction;

import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.api.flag.node.FlagNodeLiteral;
import me.doggy.heavyguard.api.interaction.*;
import me.doggy.heavyguard.api.region.IRegionsProvider;
import me.doggy.heavyguard.api.utils.TextBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class InteractionHandler implements IInteractionHandler
{
    private final IRegionsProvider _regionsProvider;
    
    public InteractionHandler(IRegionsProvider regionsProvider)
    {
        _regionsProvider = regionsProvider;
    }
    
    private void trySendNoAccessMessage(Interaction interaction, BaseComponent component)
    {
        if(interaction instanceof IInteractedByEntities interactedByEntity)
        {
            for(var entity : interactedByEntity.getInteractors())
                if(entity instanceof Player player)
                    TextBuilder.of(component, ChatFormatting.RED).send(player, ChatType.GAME_INFO);
        }
    }
    
    public InteractionResult test(Interaction interaction)
    {
        Objects.requireNonNull(interaction);
        
        var result = interaction.test(_regionsProvider);
        
        if(result.isCancelled())
            trySendNoAccessMessage(interaction, result.getCancelInfo());
        
        return result;
    }
}
