package me.doggy.heavyguard.util;

public class ReflectionHelper
{
    public static <P, C extends P> Class<P> getSuperClassUntil(Class<C> clazz, Class<P> until)
    {
        Class<?> current = clazz;
        while(current.equals(until) == false)
            current = current.getSuperclass();
        return (Class<P>)current;
    }
}
