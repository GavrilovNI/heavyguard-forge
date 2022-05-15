package me.doggy.heavyguard.util;


import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class VectorUtils
{
    public static Vec3i doubleToInt(Vec3 vector)
    {
        return new Vec3i((int)vector.x, (int)vector.y, (int)vector.z);
    }
    
    public static Vec3i doubleToInt(Vec3 vector, Function<Double, Integer> transform)
    {
        return new Vec3i(transform.apply(vector.x), transform.apply(vector.y), transform.apply(vector.z));
    }
}
