package me.doggy.heavyguard.command.argument;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.doggy.heavyguard.HeavyGuard;
import net.minecraft.network.chat.TranslatableComponent;

public class ModCommandExceptions
{
    public static final DynamicCommandExceptionType REGION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(
            foo -> new TranslatableComponent(HeavyGuard.MOD_ID + ".argument.region.notfound"));
    public static final DynamicCommandExceptionType REGION_FOUND_TO_MANY = new DynamicCommandExceptionType(
            foo -> new TranslatableComponent(HeavyGuard.MOD_ID + ".argument.region.foundtomany"));
    public static final DynamicCommandExceptionType PLAYER_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(
            name -> new TranslatableComponent(HeavyGuard.MOD_ID + ".argument.player.notfound", name));
    public static final DynamicCommandExceptionType MISSED_ARGUMENT_EXCEPTION = new DynamicCommandExceptionType(
            cursorPosition -> new TranslatableComponent(HeavyGuard.MOD_ID + ".argument.missed", cursorPosition));
    public static final DynamicCommandExceptionType NOT_FINISHED_ARGUMENT_EXCEPTION = new DynamicCommandExceptionType(
            cursorPosition -> new TranslatableComponent(HeavyGuard.MOD_ID + ".argument.notfinished", cursorPosition));
    public static final DynamicCommandExceptionType UNKNOWN_ARGUMENT_VALUE = new DynamicCommandExceptionType(
            cursorPosition -> new TranslatableComponent(HeavyGuard.MOD_ID + ".argument.unknown", cursorPosition));
}
