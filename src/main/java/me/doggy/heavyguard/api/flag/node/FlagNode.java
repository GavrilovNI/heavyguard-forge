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
    public static<T> ArrayList<String> getClassPath(Class<T> clazz, Class<? super T> until)
    {
        ArrayList<String> result = new ArrayList<>();
        Class<? super T> current = clazz;
        while(current.equals(until) == false)
        {
            result.add(classToFlagNodeName(current));
            current = current.getSuperclass();
        }
        return result;
    }
}
