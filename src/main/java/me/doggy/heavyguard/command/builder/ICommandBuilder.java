package me.doggy.heavyguard.command.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import javax.annotation.Nullable;
import java.util.Arrays;

public interface ICommandBuilder
{
    // it's not a deep copy
    CommandBuilder copy();
    CommandBuilder then(CommandBuilder commandBuilder, boolean optional);
    CommandBuilder optional(CommandNode<CommandSourceStack> optionalNode);
    CommandBuilder alias(CommandNode<CommandSourceStack> aliasNode);
    CommandBuilder aliases(CommandNode<CommandSourceStack>... aliasNodes);
    CommandBuilder executes(Command<CommandSourceStack> command);
    CommandBuilder then(CommandNode<CommandSourceStack> node, boolean optional);
    @Nullable
    LiteralCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher);
    
    
    default CommandBuilder then(CommandBuilder commandBuilder)
    {
        return then(commandBuilder, false);
    }
    
    default CommandBuilder literal(String literal, boolean optional)
    {
        return then(Commands.literal(literal).build(), optional);
    }
    
    default CommandBuilder literal(String literal)
    {
        return literal(literal, false);
    }
    
    default <T> CommandBuilder argument(String name, ArgumentType<T> type, boolean optional)
    {
        return then(Commands.argument(name, type).build(), optional);
    }
    
    default <T> CommandBuilder argument(String name, ArgumentType<T> type)
    {
        return argument(name, type, false);
    }
    
    default CommandBuilder alias(String alias)
    {
        return alias(Commands.literal(alias).build());
    }
    
    default CommandBuilder aliases(String... aliases)
    {
        return aliases(
                Arrays.stream(aliases).map(a -> Commands.literal(a).build()).toArray(LiteralCommandNode[]::new));
    }
    
    default <T> CommandBuilder alias(String name, ArgumentType<T> type)
    {
        return alias(Commands.argument(name, type).build());
    }
}
