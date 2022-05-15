package me.doggy.heavyguard.region;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.event.region.RegionsEvent;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.flag.RegionFlags;
import me.doggy.heavyguard.util.ExecutionStats;
import me.doggy.heavyguard.util.CompoundTagHelper;
import me.doggy.heavyguard.util.LevelUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class ServerLevelRegionsLoader
{
    public static class RegionSaveLoadException extends Exception
    {
        public RegionSaveLoadException(String message)
        {
            super(message);
        }
    }
    
    private static final String REGION_MAIN_FILE_EXTENSION = "dat";
    
    private static File getRegionMainFile(File regionFolder)
    {
        return new File(regionFolder, "data." + REGION_MAIN_FILE_EXTENSION);
    }
    
    private static File getRegionsDir(ServerLevel world)
    {
        File worldDir = LevelUtils.getDirectory(world);
        return worldDir.toPath().resolve(HeavyGuard.MOD_ID).resolve("regions").toFile();
    }
    
    public static Set<String> removeRegionsFromFiles(ServerLevel world, Set<String> regionNames)
    {
        Set<String> removedRegions = new HashSet<>();
        File worldDir = getRegionsDir(world);
        if(worldDir.exists() == false)
        {
            removedRegions.addAll(regionNames);
            return removedRegions;
        }
        for(var regionDir : worldDir.listFiles(f -> f.isDirectory() && regionNames.contains(f.getName())))
        {
            try
            {
                FileUtils.deleteDirectory(regionDir);
                removedRegions.add(regionDir.getName());
            }
            catch(IOException e)
            {
                HeavyGuard.LOGGER.error("Couldn't remove region folder: " + regionDir.getName());
                e.printStackTrace();
            }
        }
        return removedRegions;
    }
    
    protected static ExecutionStats saveRegions(IRegionsContainer regionsContainer)
    {
        var level = regionsContainer.getLevel();
        File levelDir = getRegionsDir(level);
        ExecutionStats stats = new ExecutionStats();
        
        for(var region : regionsContainer)
        {
            File regionDir = new File(levelDir, region.getName());
            if(regionDir.exists() == false || region.isDirty())
            {
                try
                {
                    saveRegion(region, regionDir);
                    stats.success++;
                }
                catch(IOException | RegionSaveLoadException e)
                {
                    stats.failed++;
                    HeavyGuard.LOGGER.warn("Couldn't save region '" + region.getName() + "' in level " + LevelUtils.getName(level) + ".");
                    e.printStackTrace();
                }
            }
            else
            {
                stats.skipped++;
            }
        }
    
        RegionEvent.postEventBy(regionsContainer, new RegionsEvent.Saved(regionsContainer));
        return stats;
    }
    
    protected static ExecutionStats loadRegions(IRegionsContainer regionsContainer)
    {
        var level = regionsContainer.getLevel();
        File regionsDir = getRegionsDir(level);
        ExecutionStats stats = new ExecutionStats();
        if(regionsDir.exists() == false)
        {
            regionsContainer.clear();
            return stats;
        }
    
        regionsContainer.clear();
        for(var regionDir : regionsDir.listFiles(f -> f.isDirectory()))
        {
            try
            {
                LevelRegion loadedRegion = tryLoadRegion(regionDir);
                regionsContainer.addRegion(loadedRegion);
                RegionEvent.postEventBy(regionsContainer, new RegionEvent.Loaded(loadedRegion));
                stats.success++;
            }
            catch(IOException | RegionSaveLoadException e)
            {
                stats.failed++;
                HeavyGuard.LOGGER.warn("Couldn't load region '" + regionDir.getName() + "' in level " + LevelUtils.getName(level) + ".");
                e.printStackTrace();
            }
        }
    
        RegionEvent.postEventBy(regionsContainer, new RegionsEvent.Loaded(regionsContainer));
        return stats;
    }
    
    
    @Nullable
    public static void saveRegion(IRegion region, File regionDirectory) throws IOException, RegionSaveLoadException
    {
        if(regionDirectory.getName().equals(region.getName()) == false)
            throw new RegionSaveLoadException("regionDirectory name not equal to region name");
        regionDirectory.mkdirs();
        
        File mainFile = getRegionMainFile(regionDirectory);
        
        CompoundTag mainNbt = new CompoundTag();
        
        mainNbt.putString("name", region.getName());
        CompoundTagHelper.putLevel(mainNbt, "world", region.getLevel());
        region.getMembers().toNbt(mainNbt, "members");
        
        if(region instanceof LevelBoundedRegion)
            CompoundTagHelper.putBoundsInt(mainNbt, "bounds", ((LevelBoundedRegion)region)._bounds);
        
        NbtIo.writeCompressed(mainNbt, mainFile);
        
        File flagsFile = new File(regionDirectory, "flags.json");
        
        Gson gson = new GsonBuilder().registerTypeAdapter(RegionFlags.class, new RegionFlags.Serializer()).setPrettyPrinting().create();
        
        Files.writeString(flagsFile.toPath(), gson.toJson(region.getFlags()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        if(region instanceof ICleanable cleanable)
            cleanable.markClean();
        RegionEvent.postEventBy(region, new RegionEvent.Saved(region));
    }
    
    
    
    private static RegionFlags loadRegionFlags(File regionDirectory, RegionFlags defaultFlags)
    {
        File flagsFile = new File(regionDirectory, "flags.json");
    
        Gson gson = new GsonBuilder().registerTypeAdapter(RegionFlags.class, new RegionFlags.Serializer()).create();
        JsonElement flagsElement = null;
        try
        {
            flagsElement = JsonParser.parseReader(new FileReader(flagsFile.getAbsolutePath()));
            return gson.fromJson(flagsElement, RegionFlags.class);
        }
        catch(FileNotFoundException e)
        {
            HeavyGuard.LOGGER.warn("Region flags file not found. (region: '" + regionDirectory.getName() + "')");
            return defaultFlags;
        }
    }
    
    private static LevelRegion tryLoadRegion(File regionDirectory) throws RegionSaveLoadException, IOException
    {
        if(regionDirectory == null)
            throw new NullPointerException("regionDirectory is null");
        if(regionDirectory.exists() == false || regionDirectory.isDirectory() == false)
            throw new RegionSaveLoadException("Region folder doesn't exist.");
        File mainFile = getRegionMainFile(regionDirectory);
        if(mainFile.exists() == false)
            throw new RegionSaveLoadException("Region main file doesn't exist.");
        
        CompoundTag mainNbt = NbtIo.readCompressed(mainFile);
        
        final String[] requiredNbtKeys = new String[]{"name", "world"};
        for(var key : requiredNbtKeys)
            if(mainNbt.contains(key) == false)
                throw new RegionSaveLoadException("Wrong data in main file.");
    
        ServerLevel world = CompoundTagHelper.getServerLevel(mainNbt, "world");
        String name = mainNbt.getString("name");
        if(name.equals(regionDirectory.getName()) == false)
            throw new RegionSaveLoadException(
                    "Wrong region name '" + name + "' in main file. (world: '" + LevelUtils.getName(
                            world) + "', region: '" + regionDirectory.getName() + "')");
        
        BoundsInt bounds = mainNbt.contains("bounds") ? CompoundTagHelper.getBoundsInt(mainNbt, "bounds") : null;
        RegionFlags flags = loadRegionFlags(regionDirectory, new RegionFlags());
        RegionMembers members = mainNbt.contains("members") ? RegionMembers.fromNbt(mainNbt,
                "members") : new RegionMembers();
        
        LevelRegion region = bounds == null ? new LevelRegion(name, world, flags,
                members) : new LevelBoundedRegion(name, world, flags, members, bounds);
        
        region.markClean();
        return region;
    }
}
