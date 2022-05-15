package me.doggy.heavyguard.command.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.util.ReflectionHelper;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class CommandBuilder implements ICommandBuilder
{
    private final CommandNode<CommandSourceStack> _root;
    private CommandNode<CommandSourceStack> _current;
    
    private final ArrayList<CommandNode<CommandSourceStack>> _headAliases;
    
    private final ArrayList<CommandNode<CommandSourceStack>> _prevNodes;
    private final ArrayList<CommandNode<CommandSourceStack>> _currentAliases;
    private final ArrayList<CommandNode<CommandSourceStack>> _currentOptionalNodes;
    
    private <T> CommandBuilder(CommandNode<CommandSourceStack> node)
    {
        Objects.requireNonNull(node);
        if(node.getChildren().isEmpty() == false)
            throw new IllegalStateException("Cannot create builder from node with children.");
        _root = node;
        _current = _root;
        _headAliases = new ArrayList<>();
        _prevNodes = new ArrayList<>();
        _currentAliases = new ArrayList<>();
        _currentOptionalNodes = new ArrayList<>();
    }
    private <T> CommandBuilder(CommandBuilder other)
    {
        Objects.requireNonNull(other);
        _root = other._root;
        _current = other._current;
        _headAliases = new ArrayList<>(other._headAliases);
        _prevNodes = new ArrayList<>(other._prevNodes);
        _currentAliases = new ArrayList<>(other._currentAliases);
        _currentOptionalNodes = new ArrayList<>(other._currentOptionalNodes);
    }
    
    public static CommandBuilder of(CommandNode<CommandSourceStack> node)
    {
        return new CommandBuilder(node);
    }
    
    public static CommandBuilder of(String literal)
    {
        return of(Commands.literal(literal).build());
    }
    
    public static <T> CommandBuilder of(String name, ArgumentType<T> type)
    {
        return of(Commands.argument(name, type).build());
    }
    
    
    // it's not a deep copy
    @Override
    public CommandBuilder copy()
    {
        return new CommandBuilder(this);
    }
    
    @Override
    public CommandBuilder then(CommandBuilder commandBuilder, boolean optional)
    {
        if(optional)
        {
            addAsChildToFrontNodes(commandBuilder._root);
            _currentOptionalNodes.remove(commandBuilder._root);
            for(var optionalNode : commandBuilder._currentOptionalNodes)
                addAsChildToFrontNodes(optionalNode);
            for(var aliasNode : commandBuilder._currentAliases)
                addAsChildToFrontNodes(aliasNode);
            
            //need to add all of these for next executes() or optional() calls
            _currentOptionalNodes.add(commandBuilder._current);
            setCommand(commandBuilder._current, _current.getCommand());
            for(var aliasNode : commandBuilder._currentAliases)
            {
                _currentOptionalNodes.add(aliasNode);
                setCommand(aliasNode, _current.getCommand());
            }
            for(var optionalNode : commandBuilder._currentOptionalNodes)
            {
                _currentOptionalNodes.add(optionalNode);
                setCommand(optionalNode, _current.getCommand());
            }
        }
        else
        {
            thenInner(commandBuilder._root);
            for(var aliasNode : commandBuilder._headAliases)
                _prevNodes.forEach(n -> n.addChild(aliasNode));
            
            _current = commandBuilder._current;
        }
        return this;
    }
    
    private CommandBuilder then(CommandNode<CommandSourceStack> node)
    {
        if(node.getChildren().isEmpty() == false)
            throw new IllegalStateException("Cannot add node with children.");
        return thenInner(node);
    }
    
    private CommandBuilder thenInner(CommandNode<CommandSourceStack> node)
    {
        _prevNodes.clear();
        _prevNodes.add(_current);
        _prevNodes.addAll(_currentAliases);
        _prevNodes.addAll(_currentOptionalNodes);
        _prevNodes.forEach(n -> n.addChild(node));
        
        _currentAliases.clear();
        _currentOptionalNodes.clear();
        _current = node;
        return this;
    }
    
    private void addAsChildToFrontNodes(CommandNode<CommandSourceStack> node)
    {
        _current.addChild(node);
        for(var optionalNode : _currentOptionalNodes)
            optionalNode.addChild(node);
        for(var aliasNode : _currentAliases)
            aliasNode.addChild(node);
    }
    
    @Override
    public CommandBuilder optional(CommandNode<CommandSourceStack> optionalNode)
    {
        addAsChildToFrontNodes(optionalNode);
        _currentOptionalNodes.add(optionalNode);
        
        return this;
    }
    
    @Override
    public CommandBuilder alias(CommandNode<CommandSourceStack> aliasNode)
    {
        if(_prevNodes.isEmpty())
            _headAliases.add(aliasNode);
        else
        {
            _prevNodes.forEach(n -> n.addChild(aliasNode));
        }
        
        var lastChildren = getLastChildren(aliasNode);
        
        var command = _current.getCommand();
        for(var child : lastChildren)
        {
            if(child.getCommand() != null)
                HeavyGuard.LOGGER.warn(
                        "One of last children " + child.toString() + " of alias has an execution command. The command will be set to current command builder last node " + _current.toString() + " command. If you want have different execution command use separate CommandBuilder and set same next node for both builders");
            setCommand(child, command);
            _currentAliases.add(child);
            for(var optionalNode : _currentOptionalNodes)
                child.addChild(optionalNode);
        }
        
        return this;
    }
    
    @Override
    public CommandBuilder aliases(CommandNode<CommandSourceStack>... aliasNodes)
    {
        for(var aliasNode : aliasNodes)
            alias(aliasNode);
        return this;
    }
    
    @Override
    public CommandBuilder executes(Command<CommandSourceStack> command)
    {
        setCommand(_current, command);
        for(var optionalNode : _currentOptionalNodes)
            setCommand(optionalNode, command);
        for(var aliasNode : _currentAliases)
            setCommand(aliasNode, command);
        return this;
    }
    
    @Override
    public CommandBuilder then(CommandNode<CommandSourceStack> node, boolean optional)
    {
        if(optional)
            return optional(node);
        else
            return thenInner(node);
    }
    
    @Override
    @Nullable
    public LiteralCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> result = null;
        if(_root instanceof LiteralCommandNode<CommandSourceStack> == false)
            HeavyGuard.LOGGER.warn(
                    "Command manager head " + _root.toString() + " isn't a literal, can't register. Skipping...");
        else
        {
            result = (LiteralCommandNode<CommandSourceStack>)_root;
            registerNode(dispatcher, result);
        }
        
        for(var aliasNode : _headAliases)
        {
            if(aliasNode instanceof LiteralCommandNode<CommandSourceStack> == false)
                HeavyGuard.LOGGER.warn(
                        "Command manager head alias " + _root.toString() + " isn't a literal, can't register. Skipping...");
            else
            {
                LiteralCommandNode<CommandSourceStack> literalAliasNode = (LiteralCommandNode<CommandSourceStack>)aliasNode;
                registerNode(dispatcher, literalAliasNode);
                if(result == null)
                    result = literalAliasNode;
            }
        }
        return result;
    }
    
    private static HashSet<CommandNode<CommandSourceStack>> getLastChildren(CommandNode<CommandSourceStack> node)
    {
        return getLastChildren(node, new HashSet<>());
    }
    
    private static HashSet<CommandNode<CommandSourceStack>> getLastChildren(CommandNode<CommandSourceStack> node, HashSet<CommandNode<CommandSourceStack>> lastChildren)
    {
        var children = node.getChildren();
        if(children.isEmpty())
        {
            lastChildren.add(node);
            return lastChildren;
        }
        else
        {
            for(var child : children)
                getLastChildren(child, lastChildren);
            return lastChildren;
        }
    }
    
    private static void setCommand(CommandNode<CommandSourceStack> node, Command<CommandSourceStack> command)
    {
        try
        {
            Field field = ReflectionHelper.getSuperClassUntil(node.getClass(), CommandNode.class).getDeclaredField(
                    "command");
            field.setAccessible(true);
            field.set(node, command);
        }
        catch(IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }
    
    private static void setRedirect(CommandNode<CommandSourceStack> node, CommandNode<CommandSourceStack> redirect)
    {
        if(node.getChildren().isEmpty() == false)
            throw new IllegalStateException("Cannot forward a node with children");
        try
        {
            Class<CommandNode> clazz = ReflectionHelper.getSuperClassUntil(node.getClass(), CommandNode.class);
            Field fieldRedirect = clazz.getDeclaredField("redirect");
            Field fieldModifier = clazz.getDeclaredField("modifier");
            Field fieldForks = clazz.getDeclaredField("forks");
            fieldRedirect.setAccessible(true);
            fieldModifier.setAccessible(true);
            fieldForks.setAccessible(true);
            fieldRedirect.set(node, redirect);
            fieldModifier.set(node, null);
            fieldForks.set(node, false);
        }
        catch(IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }
    
    private static <S> void registerNode(CommandDispatcher<CommandSourceStack> dispatcher, LiteralCommandNode<S> node)
    {
        try
        {
            Field field = ReflectionHelper.getSuperClassUntil(dispatcher.getClass(),
                    CommandDispatcher.class).getDeclaredField("root");
            field.setAccessible(true);
            ((RootCommandNode<S>)field.get(dispatcher)).addChild(node);
        }
        catch(IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }
}
