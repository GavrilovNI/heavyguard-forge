package me.doggy.heavyguard.util;

import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.util.delegates.Consumer2;
import me.doggy.heavyguard.util.delegates.Consumer3;
import me.doggy.heavyguard.util.delegates.Function2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CompoundTagHelper
{
    public static void putVec3i(CompoundTag nbt, String key, Vec3i vec3i)
    {
        nbt.putInt(key, vec3i.getX());
        nbt.putInt(key + "Y", vec3i.getY());
        nbt.putInt(key + "Z", vec3i.getZ());
    }
    
    public static Vec3i getVec3i(CompoundTag nbt, String key)
    {
        int x = nbt.getInt(key);
        int y = nbt.getInt(key + "Y");
        int z = nbt.getInt(key + "Z");
        return new Vec3i(x, y, z);
    }
    
    public static void removeVec3i(CompoundTag nbt, String key)
    {
        nbt.remove(key);
        nbt.remove(key + "Y");
        nbt.remove(key + "Z");
    }
    
    public static void putBoundsInt(CompoundTag nbt, String key, BoundsInt bounds)
    {
        putVec3i(nbt, key, bounds.getMin());
        putVec3i(nbt, key + "M", bounds.getMax());
    }
    
    public static BoundsInt getBoundsInt(CompoundTag nbt, String key)
    {
        Vec3i min = getVec3i(nbt, key);
        Vec3i max = getVec3i(nbt, key + "M");
        return new BoundsInt(min, max);
    }
    
    public static void removeBoundsInt(CompoundTag nbt, String key)
    {
        removeVec3i(nbt, key);
        removeVec3i(nbt, key + "M");
    }
    
    public static void putLevel(CompoundTag nbt, String key, Level level)
    {
        nbt.putString(key, LevelUtils.getIdentifier(level).toString());
    }
    
    @Nullable
    public static ServerLevel getServerLevel(CompoundTag nbt, String key)
    {
        ResourceLocation identifier = new ResourceLocation(nbt.getString(key));
        return ServerGetter.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, identifier));
    }
    
    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static ClientLevel getClientLevel(CompoundTag nbt, String key)
    {
        ResourceLocation identifier = new ResourceLocation(nbt.getString(key));
        var currentClientLevel = Minecraft.getInstance().level;
        if(LevelUtils.getIdentifier(currentClientLevel).equals(identifier))
            return currentClientLevel;
        else
            return null;
    }
    
    public static <K, V> void putMap(CompoundTag nbt, String key, Map<K, V> map, Consumer3<CompoundTag, String, K> putKeyAction, Consumer3<CompoundTag, String, V> putValueAction)
    {
        nbt.putInt(key, map.size());
        int i = 0;
        for(var mapEntry : map.entrySet())
        {
            putKeyAction.apply(nbt, key + i, mapEntry.getKey());
            putValueAction.apply(nbt, key + i + "V", mapEntry.getValue());
            i++;
        }
    }
    
    public static <V> void putMap(CompoundTag nbt, String key, Map<?, V> map, Consumer3<CompoundTag, String, V> putValueAction)
    {
        putMap(nbt, key, map, (n, k, v) -> n.putString(k, v.toString()), putValueAction);
    }
    
    public static <K, V> HashMap<K, V> getMap(CompoundTag nbt, String key, Function2<CompoundTag, String, K> getKeyAction, Function2<CompoundTag, String, V> getValueAction)
    {
        int count = nbt.getInt(key);
        HashMap<K, V> result = new HashMap<>();
        for(int i = 0; i < count; i++)
        {
            K mapKey = getKeyAction.apply(nbt, key + i);
            V mapValue = getValueAction.apply(nbt, key + i + "V");
            result.put(mapKey, mapValue);
        }
        return result;
    }
    
    public static void removeMap(CompoundTag nbt, String key, Consumer2<CompoundTag, String> removeKeyAction, Consumer2<CompoundTag, String> removeValueAction)
    {
        int count = nbt.getInt(key);
        nbt.remove(key);
        for(int i = 0; i < count; i++)
        {
            removeKeyAction.apply(nbt, key + i);
            removeValueAction.apply(nbt, key + i + "V");
        }
    }
    
    public static void removeMap(CompoundTag nbt, String key)
    {
        removeMap(nbt, key, (n, k) -> n.remove(k), (n, k) -> n.remove(k));
    }
    
    public static <T extends Enum> void putEnum(CompoundTag nbt, String key, T value)
    {
        nbt.putInt(key, value.ordinal());
    }
    
    public static <T extends Enum> T getEnum(CompoundTag nbt, String key, Class<T> clazz)
    {
        return clazz.getEnumConstants()[nbt.getInt(key)];
    }
}
