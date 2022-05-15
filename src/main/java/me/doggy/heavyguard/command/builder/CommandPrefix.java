package me.doggy.heavyguard.command.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

public class CommandPrefix implements ICommandBuilder
{
    private final CommandBuilder _builder;
    
    private <T> CommandPrefix(CommandBuilder builder)
    {
        _builder = builder.copy();
    }
    public static CommandPrefix of(CommandBuilder builder)
    {
        return new CommandPrefix(builder);
    }
    
    @Override
    public CommandBuilder copy()
    {
        return _builder.copy();
    }
    
    @Override
    public CommandBuilder then(CommandBuilder commandBuilder, boolean optional)
    {
        return copy().then(commandBuilder, optional);
    }
    
    @Override
    public CommandBuilder optional(CommandNode<CommandSourceStack> optionalNode)
    {
        return copy().optional(optionalNode);
    }
    
    @Override
    public CommandBuilder alias(CommandNode<CommandSourceStack> aliasNode)
    {
        return copy().alias(aliasNode);
    }
    
    @Override
    public CommandBuilder aliases(CommandNode<CommandSourceStack>... aliasNodes)
    {
        return copy().aliases(aliasNodes);
    }
    
    @Override
    public CommandBuilder executes(Command<CommandSourceStack> command)
    {
        return copy().executes(command);
    }
    
    @Override
    public CommandBuilder then(CommandNode<CommandSourceStack> node, boolean optional)
    {
        return copy().then(node, optional);
    }
    
    @Nullable
    @Override
    public LiteralCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        return copy().register(dispatcher);
    }
}
