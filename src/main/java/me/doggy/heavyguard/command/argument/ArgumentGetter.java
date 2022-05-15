package me.doggy.heavyguard.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

public interface ArgumentGetter<T>
{
    T get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException;
}