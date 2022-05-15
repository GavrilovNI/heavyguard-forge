package me.doggy.heavyguard.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.File;
import java.lang.reflect.Field;

public class LevelUtils
{
    public static boolean areEqualByResourceLocation(Level levelA, Level levelB)
    {
        return getIdentifier(levelA).equals(getIdentifier(levelB));
    }
    
    public static File getDirectory(ServerLevel level)
    {
        return getDirectory(level.getChunkSource().getDataStorage());
    }
    
    private static File getDirectory(DimensionDataStorage dataStorage)
    {
        try
        {
            Field field = DimensionDataStorage.class.getDeclaredField("dataFolder");
            field.setAccessible(true);
            return (File)field.get(dataStorage);
        }
        catch(IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static ResourceLocation getIdentifier(Level level)
    {
        return level.dimension().location();
    }
    
    public static String getName(Level world)
    {
        return getIdentifier(world).toString();
    }
}
