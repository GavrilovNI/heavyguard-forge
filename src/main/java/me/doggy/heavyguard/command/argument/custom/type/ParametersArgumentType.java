package me.doggy.heavyguard.command.argument.custom.type;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.datafixers.util.Pair;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.command.argument.ArgumentGetter;
import me.doggy.heavyguard.command.argument.ModCommandExceptions;
import me.doggy.heavyguard.util.CommandContextArg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ParametersArgumentType implements ArgumentType<HashMap<String, Object>>
{
    private Map<String, ArgumentType<?>> _parameters;
    private Map<String, Set<String>> _restrictions;
    private List<String> _examples;
    
    public ParametersArgumentType(Map<String, ArgumentType<?>> parameters)
    {
        _parameters = parameters;
        _restrictions = new HashMap<>();
        setupExamples();
    }
    
    public ParametersArgumentType(Map<String, ArgumentType<?>> parameters, Map<String, Set<String>> restrictions)
    {
        _parameters = parameters;
        _restrictions = restrictions;
        setupExamples();
    }
    
    private void setupExamples()
    {
        _examples = new ArrayList<>();
        for(var e : _parameters.entrySet())
        {
            var examples = e.getValue().getExamples();
            if(examples.isEmpty())
                continue;
            _examples.add(String.join(" ", e.getKey(), (String)examples.iterator().next()));
        }
    }
    
    @Nullable
    public String readParameterKey(StringReader reader, final Set<String> restrictedParameters)
    {
        int returnCursorPosition = reader.getCursor();
        reader.skipWhitespace();
        String parameterKey = reader.readUnquotedString();
        if(_parameters.containsKey(parameterKey) && restrictedParameters.contains(parameterKey) == false)
        {
            return parameterKey;
        }
        else if(_parameters.containsKey("") && restrictedParameters.contains("") == false)
        {
            reader.setCursor(returnCursorPosition);
            return "";
        }
        else
        {
            reader.setCursor(returnCursorPosition);
            return null;
        }
    }
    
    @Nullable
    public String readParameterKey(StringReader reader)
    {
        return readParameterKey(reader, new HashSet<>());
    }
    
    @Nullable
    public Object readParameterValue(StringReader reader, String parameterKey) throws CommandSyntaxException
    {
        reader.skipWhitespace();
        if(reader.canRead() == false)
            throw ModCommandExceptions.NOT_FINISHED_ARGUMENT_EXCEPTION.create(reader.getCursor());
        ArgumentType argumentType = _parameters.get(parameterKey);
        return argumentType.parse(reader);
    }
    
    @Nullable
    public Pair<String, Object> parseParameter(StringReader reader, final Set<String> restrictedParameters) throws CommandSyntaxException
    {
        String parameterKey = readParameterKey(reader, restrictedParameters);
        if(parameterKey == null)
            return null;
        else
            return new Pair<>(parameterKey, readParameterValue(reader, parameterKey));
    }
    
    @Nullable
    public Pair<String, Object> parseParameter(StringReader reader) throws CommandSyntaxException
    {
        return parseParameter(reader, new HashSet<>());
    }
    
    @Override
    public HashMap<String, Object> parse(StringReader reader) throws CommandSyntaxException
    {
        HashMap<String, Object> result = new HashMap<>();
        Pair<String, Object> parameter;
        
        Set<String> restrictedParameters = new HashSet<>();
        
        while((parameter = parseParameter(reader, restrictedParameters)) != null)
        {
            String key = parameter.getFirst();
            result.put(key, parameter.getSecond());
            restrictedParameters.add(key);
            restrictedParameters.addAll(_restrictions.getOrDefault(key, new HashSet<>()));
        }
        
        return result;
    }
    
    private ArrayList<Integer> getArgumentPositions(StringReader reader)
    {
        ArrayList<Integer> result = new ArrayList<>();
        try
        {
            result.add(reader.getCursor());
            Set<String> restrictedParameters = new HashSet<>();
            Pair<String, Object> parameter;
            while((parameter = parseParameter(reader, restrictedParameters)) != null)
            {
                String key = parameter.getFirst();
                result.add(reader.getCursor());
                restrictedParameters.add(key);
                restrictedParameters.addAll(_restrictions.getOrDefault(key, new HashSet<>()));
            }
            result.remove(result.size() - 1);
        }
        catch(CommandSyntaxException ex)
        {
        }
        return result;
    }
    
    @Override
    public Collection<String> getExamples()
    {
        return _examples;
    }
    
    @Nullable
    private <S> Collection<CommandNode<S>> getChildNodes(CommandContext<S> context)
    {
        CommandNode<S> result = context.getRootNode();
        for(var node : context.getNodes())
        {
            result = result.getChild(node.getNode().getName());
        }
        var children = result.getChildren().stream().toList();
        return children.size() > 0 ? children.get(0).getChildren() : children;
    }
    
    private String getLastKeyIfNotRestricted(StringReader reader)
    {
        ArrayList<Integer> argumentPositions = getArgumentPositions(reader);
        if(argumentPositions.isEmpty())
            return null;
        
        ArrayList<String> keys = new ArrayList<>();
        for(int i = 0; i < argumentPositions.size(); i++)
        {
            reader.setCursor(argumentPositions.get(i));
            String key = readParameterKey(reader);
            keys.add(key);
        }
        
        Set<String> restrictedParameters = new HashSet<>();
        for(int i = 0; i < keys.size() - 1; i++)
        {
            String key = keys.get(i);
            restrictedParameters.add(key);
            restrictedParameters.addAll(_restrictions.getOrDefault(key, new HashSet<>()));
        }
        
        String lastKey = keys.get(keys.size() - 1);
        if(restrictedParameters.contains(lastKey))
            return null;
        
        return lastKey;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        final List<String> suggestions = new ArrayList<>();
        
        String currentArgument = builder.getRemaining();
        StringReader reader = new StringReader(currentArgument);
        String lastKey = getLastKeyIfNotRestricted(reader);
        if(lastKey == null)
            return SharedSuggestionProvider.suggest(suggestions, builder); // key restricted
        
        for(var e : _parameters.entrySet())
        {
            String realKey = e.getKey();
            if(realKey.startsWith(lastKey) && (realKey.isEmpty() || lastKey.isEmpty() == false))
            {
                int skipCount = reader.getCursor() + (reader.canRead() ? 1 : 0);
                
                SuggestionsBuilder suggestionsBuilder = new SuggestionsBuilder(builder.getInput(),
                        builder.getStart() + skipCount);
                
                List<Suggestion> argumentSuggestions;
                try
                {
                    argumentSuggestions = e.getValue().listSuggestions(context, suggestionsBuilder).get(1,
                            TimeUnit.SECONDS).getList();
                }
                catch(InterruptedException | ExecutionException | TimeoutException ex)
                {
                    continue;
                }
                
                int subLength = reader.getCursor() - lastKey.length();
                String prefix = realKey.isEmpty() ? "" : realKey + " ";
                argumentSuggestions.forEach(
                        s -> suggestions.add(currentArgument.substring(0, subLength) + prefix + s.getText()));
            }
        }
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }
    
    public static ParametersArgumentType parameter(String parameterKey, ArgumentType<?> argumentType)
    {
        return new ParametersArgumentType(Map.of(parameterKey, argumentType));
    }
    
    public static ParametersArgumentType parameters(Map<String, ArgumentType<?>> parameters)
    {
        return new ParametersArgumentType(parameters);
    }
    
    public static ParametersArgumentType parameters(Map<String, ArgumentType<?>> parameters, Map<String, Set<String>> restrictions)
    {
        return new ParametersArgumentType(parameters, restrictions);
    }
    
    @Nullable
    public static HashMap<String, Object> getParameters(CommandContext<CommandSourceStack> context, String name)
    {
        var map = CommandContextArg.getOptional(context, name, HashMap.class);
        return map == null ? new HashMap<>() : map;
    }
    
    
    @Nullable
    public static <T> T getParameter(CommandContext<CommandSourceStack> context, Map<String, Object> parameters, String key, ArgumentGetter<T> getter, @Nullable T defaultValue) throws CommandSyntaxException
    {
        var value = parameters.getOrDefault(key, null);
        if(value == null)
            return defaultValue;
        var parsedArguments = Map.of(key,
                new ParsedArgument(context.getRange().getStart(), context.getRange().getEnd(), value));
        CommandContext<CommandSourceStack> argumentContext = new CommandContext(context.getSource(),
                context.getInput(), parsedArguments, context.getCommand(), context.getRootNode(), context.getNodes(),
                context.getRange(), context.getChild(), context.getRedirectModifier(), context.isForked());
        return getter.get(argumentContext, key);
    }
    
    @Nullable
    public static <T> T getParameter(CommandContext<CommandSourceStack> context, String name, String key, ArgumentGetter<T> getter, @Nullable T defaultValue) throws CommandSyntaxException
    {
        var parameters = getParameters(context, name);
        return getParameter(context, parameters, key, getter, defaultValue);
    }
    
    @Nullable
    public static <T> T getParameter(CommandContext<CommandSourceStack> context, String name, String key, ArgumentGetter<T> getter) throws CommandSyntaxException
    {
        return getParameter(context, name, key, getter, null);
    }
    
    @Nullable
    public static <T> T getParameter(CommandContext<CommandSourceStack> context, Map<String, Object> parameters, String key, ArgumentGetter<T> getter) throws CommandSyntaxException
    {
        return getParameter(context, parameters, key, getter, null);
    }
    
    public static void register()
    {
        ArgumentTypes.register(new ResourceLocation(HeavyGuard.MOD_ID, "parameters").toString(), ParametersArgumentType.class,
                new Serializer());
    }
    
    public static class Serializer implements ArgumentSerializer<ParametersArgumentType>
    {
        @Override
        public void serializeToNetwork(ParametersArgumentType parametersArgumentType, FriendlyByteBuf packetByteBuf)
        {
            packetByteBuf.writeMap(parametersArgumentType._parameters, (p, s) -> p.writeUtf(s),
                    (p, t) -> p.writeUtf(t.getClass().getName()));
        }
    
        @Override
        public ParametersArgumentType deserializeFromNetwork(FriendlyByteBuf packetByteBuf)
        {
            Map<String, ArgumentType<?>> result = packetByteBuf.readMap(b -> b.readUtf(), b -> {
                try
                {
                    Class<?> clazz = Class.forName(b.readUtf());
                    Constructor<?> constructor = clazz.getConstructor();
                    return (ArgumentType)constructor.newInstance();
                }
                catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e)
                {
                    HeavyGuard.LOGGER.warn("Couldn't parse ParametersArgumentType parameter - skipping.");
                    return null;
                }
            });
            
            result.values().removeAll(Collections.singleton(null));
            return new ParametersArgumentType(result);
        }
    
        @Override
        public void serializeToJson(ParametersArgumentType parameterArgumentType, JsonObject jsonObject)
        {
            jsonObject.addProperty("count", parameterArgumentType._parameters.size());
            parameterArgumentType._parameters.forEach((k, t) -> jsonObject.addProperty(k, t.getClass().getName()));
        }
    }
}
