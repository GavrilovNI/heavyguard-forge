package me.doggy.heavyguard.util;

import io.netty.buffer.Unpooled;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;

public class FriendlyByteBufHelper
{
    public static FriendlyByteBuf create()
    {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
    
    public static<T extends Enum<T>> void writeEnum(FriendlyByteBuf buf, T value)
    {
        buf.writeInt(value.ordinal());
    }
    
    public static<T extends Enum<T>> T readEnum(FriendlyByteBuf buf, Class<T> enumClass)
    {
        var ordinal = buf.readInt();
        return enumClass.getEnumConstants()[ordinal];
    }
    
    
    public static void writeBoundsInt(FriendlyByteBuf buf, BoundsInt bounds)
    {
        var min = bounds.getMin();
        var max = bounds.getMax();
        buf.writeInt(min.getX());
        buf.writeInt(min.getY());
        buf.writeInt(min.getZ());
        buf.writeInt(max.getX());
        buf.writeInt(max.getY());
        buf.writeInt(max.getZ());
    }
    
    public static BoundsInt readBoundsInt(FriendlyByteBuf buf)
    {
        Vec3i min = new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
        Vec3i max = new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
        return new BoundsInt(min, max);
    }
}
