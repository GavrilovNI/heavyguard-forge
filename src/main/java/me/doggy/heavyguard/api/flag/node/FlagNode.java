package me.doggy.heavyguard.api.flag.node;

import me.doggy.heavyguard.api.region.IRegion;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class FlagNode
{
    public abstract String getName();
    public abstract ArrayList<String> getAliases(@Nullable IRegion region);
    
    public static String classToFlagNodeName(Class clazz)
    {
        String className = clazz.getSimpleName();
        var splitted = Arrays.stream(className.split("(?=\\p{Lu})")).map(x -> x.toLowerCase()).toList();
        return String.join("_", splitted);
    }
    public static<T> ArrayList<String> getClassPathBefore(Class<T> clazz, Class<? super T> before)
    {
        ArrayList<String> result = getClassPathUntil(clazz, before);
        return new ArrayList<>(result.subList(0, result.size() - 1));
    }
    public static<T> ArrayList<String> getClassPathUntil(Class<T> clazz, Class<? super T> until)
    {
        ArrayList<String> result = new ArrayList<>();
        Class<? super T> current = clazz;
        while(until.isAssignableFrom(current))
        {
            result.add(classToFlagNodeName(current));
            current = current.getSuperclass();
        }
        return result;
    }
}
