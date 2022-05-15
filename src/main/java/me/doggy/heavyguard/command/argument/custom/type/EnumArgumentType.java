package me.doggy.heavyguard.command.argument.custom.type;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.command.argument.ModCommandExceptions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<Enum<T>>
{
    private Class<T> _enumClass;
    
    public EnumArgumentType(Class<T> enumClass)
    {
        _enumClass = enumClass;
    }
    
    @Override
    public T parse(StringReader reader) throws CommandSyntaxException
    {
        int beginCursorPosition = reader.getCursor();
        String enumValueString = reader.readUnquotedString();
        if(enumValueString.isEmpty())
            throw ModCommandExceptions.MISSED_ARGUMENT_EXCEPTION.create(reader.getCursor());
        
        for(T enumValue : _enumClass.getEnumConstants())
        {
            if(enumValue.name().equalsIgnoreCase(enumValueString))
                return enumValue;
        }
        throw ModCommandExceptions.UNKNOWN_ARGUMENT_VALUE.create(beginCursorPosition);
    }
    
    @Override
    public Collection<String> getExamples()
    {
        return Arrays.stream(_enumClass.getEnumConstants()).map(e -> e.name()).toList();
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return SharedSuggestionProvider.suggest(getExamples(), builder);
    }
    
    public static <T extends Enum<T>> EnumArgumentType<T> enum$(Class<T> enumClass)
    {
        return new EnumArgumentType(enumClass);
    }
    
    public static <T extends Enum<T>> T getEnum(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        return (T)context.getArgument(name, Enum.class);
    }
    
    public static void register()
    {
        ArgumentTypes.register(new ResourceLocation(HeavyGuard.MOD_ID, "enum").toString(), EnumArgumentType.class,
                new Serializer());
    }
    
    public static class Serializer<T extends Enum<T>> implements ArgumentSerializer<EnumArgumentType<T>>
    {
        @Override
        public void serializeToNetwork(EnumArgumentType<T> enumArgumentType, FriendlyByteBuf packetByteBuf)
        {
            packetByteBuf.writeUtf(enumArgumentType._enumClass.getName());
        }
    
        @Override
        public EnumArgumentType<T> deserializeFromNetwork(FriendlyByteBuf packetByteBuf)
        {
            String className = packetByteBuf.readUtf();
            try
            {
                return new EnumArgumentType(Class.forName(className));
            }
            catch(ClassNotFoundException e)
            {
                return new EnumArgumentType<>(null);
            }
        }
    
        @Override
        public void serializeToJson(EnumArgumentType<T> enumArgumentType, JsonObject jsonObject)
        {
            jsonObject.addProperty("enum", enumArgumentType._enumClass.getName());
        }
    }
}
