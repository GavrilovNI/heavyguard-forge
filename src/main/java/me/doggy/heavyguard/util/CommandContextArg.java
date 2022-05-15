package me.doggy.heavyguard.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;

public class CommandContextArg
{
    private static <S> Map<String, ParsedArgument<S, ?>> getArguments(final CommandContext<S> context)
    {
        Field field = null;
        try
        {
            field = ReflectionHelper.getSuperClassUntil(context.getClass(), CommandContext.class).getDeclaredField(
                    "arguments");
            field.setAccessible(true);
            var result = field.get(context);
            field.setAccessible(false);
            return (Map<String, ParsedArgument<S, ?>>)result;
        }
        catch(NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    @Nullable
    public static <V> V getOptional(final CommandContext<?> context, final String name, final Class<V> clazz)
    {
        if(getArguments(context).containsKey(name) == false)
            return null;
        return context.getArgument(name, clazz);
    }
}
