package me.doggy.heavyguard.api.utils;

import me.doggy.heavyguard.util.ReflectionHelper;
import me.doggy.heavyguard.util.delegates.Consumer2;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public interface ISendable
{
    <T>void send(T object, Consumer2<T, Component> sendFunc);
    
    default void sendSuccess(CommandSourceStack commandSource, boolean pAllowLogging)
    {
        send(commandSource, (source, text) -> source.sendSuccess(text, pAllowLogging));
    }
    
    default void sendFailure(CommandSourceStack commandSource)
    {
        send(commandSource, (source, text) -> source.sendFailure(text));
    }
    
    default void send(CommandSourceStack commandSource)
    {
        send(commandSource, Util.NIL_UUID);
    }
    
    default void send(CommandSourceStack commandSource, UUID senderUuid)
    {
        try
        {
            Field field = ReflectionHelper.getSuperClassUntil(commandSource.getClass(),
                    CommandSourceStack.class).getDeclaredField("source");
            field.setAccessible(true);
            CommandSource commandSourceInner = (CommandSource)field.get(commandSource);
            send(commandSourceInner, (source, text) -> source.sendMessage(text, senderUuid));
        }
        catch(IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }
    
    // for non server players chatType is ignored (actually, by default sendMessage for non server players is an empty method)
    default void send(Player player, ChatType chatType, UUID senderUuid)
    {
        if(player instanceof ServerPlayer serverPlayer)
            send(serverPlayer, (source, text) -> source.sendMessage(text, chatType, senderUuid));
        else
            send(player, (source, text) -> source.sendMessage(text, senderUuid));
    }
    
    // see send(Player player, ChatType chatType, UUID senderUuid)
    default void send(Player player, ChatType chatType)
    {
        send(player, chatType, Util.NIL_UUID);
    }
    
    default void send(Player player)
    {
        send(player, ChatType.CHAT);
    }
}
