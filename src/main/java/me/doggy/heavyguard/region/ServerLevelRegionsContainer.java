package me.doggy.heavyguard.region;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.event.region.BoundedRegionEvent;
import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IBoundedRegion;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.math3d.MultiMap3D;
import me.doggy.heavyguard.util.LevelUtils;
import me.doggy.heavyguard.util.VectorUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerLevelRegionsContainer implements IRegionsContainer
{
    private final IEventBus _eventBus = BusBuilder.builder().build();
    
    private final HashSet<String> _removedRegionNames = new HashSet<>();
    
    private final Map<String, IRegion> _regionsByName = new HashMap<>();
    private final MultiMap3D<IBoundedRegion> _regionsByPosition = new MultiMap3D<>(128);
    private final Set<IRegion> _globalRegions = new HashSet<>();
    
    private final ServerLevel _level;
    
    public ServerLevelRegionsContainer(ServerLevel level)
    {
        _level = level;
    }
    
    @SubscribeEvent
    public void onRegionBoundsUpdated(BoundedRegionEvent.BoundsUpdated event)
    {
        var region = event.getRegion();
        if(hasRegion(region))
        {
            var oldBounds = event.getOldBounds();
            _regionsByPosition.remove(oldBounds, region);
            _regionsByPosition.put(region.getBounds(), region);
        }
    }
    
    @Override
    public IEventBus getEventBus()
    {
        return _eventBus;
    }
    
    @Override
    public ServerLevel getLevel()
    {
        return _level;
    }
    
    public void load()
    {
        HeavyGuard.LOGGER.info("Loading regions in world " + LevelUtils.getName(getLevel()));
        var stats = ServerLevelRegionsLoader.loadRegions(this);
        HeavyGuard.LOGGER.info("Regions loaded in world " + LevelUtils.getName(getLevel()) + " " + stats);
    }
    
    public void save()
    {
        HeavyGuard.LOGGER.info("Saving regions in world " + LevelUtils.getName(getLevel()));
        
        var removedRegions = ServerLevelRegionsLoader.removeRegionsFromFiles(getLevel(), _removedRegionNames);
        _removedRegionNames.removeAll(removedRegions);
        if(_removedRegionNames.isEmpty() == false)
            HeavyGuard.LOGGER.error("Couldn't remove some region folders on save: " + _removedRegionNames.stream().toList());
        
        var stats = ServerLevelRegionsLoader.saveRegions(this);
        
        HeavyGuard.LOGGER.info("Regions saved in world " + LevelUtils.getName(getLevel()) + " " + stats);
    }
    
    @Override
    public void addRegion(IRegion region)
    {
        String name = region.getName();
        if(_regionsByName.containsKey(name))
            throw new IllegalArgumentException("Region with same name already contains in this world.");
        if(getLevel().equals(region.getLevel()) == false)
            throw new IllegalArgumentException("Region's world is not the world you try to add in.");
        
        _removedRegionNames.remove(name);
        _regionsByName.put(name, region);
        if(region instanceof LevelBoundedRegion boundedRegion)
            _regionsByPosition.put(boundedRegion.getBounds(), boundedRegion);
        else
            _globalRegions.add(region);
        
        region.getEventBus().register(this);
        RegionEvent.postEventBy(this, new RegionEvent.Added(region));
    }
    
    @Override
    public void removeRegion(IRegion region)
    {
        if(_regionsByName.containsValue(region) == false)
            throw new IllegalArgumentException("This region does not contains in this world.");
    
        region.getEventBus().unregister(this);
        RegionEvent.postEventBy(this, new RegionEvent.Removing(region));
        
        String regionName = region.getName();
        _removedRegionNames.add(regionName);
        _regionsByName.remove(regionName);
        if(region instanceof LevelBoundedRegion boundedRegion)
            _regionsByPosition.remove(boundedRegion.getBounds(), boundedRegion);
        else
            _globalRegions.remove(region);
    }
    
    public void removeRegion(String name)
    {
        var region = _regionsByName.get(name);
        if(region == null)
            throw new IllegalArgumentException("Region with this name does not contains in this world.");
        removeRegion(region);
    }
    
    @Override
    public void clear()
    {
        _regionsByName.clear();
        _regionsByPosition.clear();
        _globalRegions.clear();
        _removedRegionNames.clear();
    }
    
    @Override
    public boolean hasRegion(IRegion region)
    {
        Objects.requireNonNull(region);
        var regionWithSameName = getRegion(region.getName());
        return region == regionWithSameName;
    }
    
    @Override
    public boolean hasRegion(String name)
    {
        return _regionsByName.containsKey(name);
    }
    
    @Override
    public Set<IRegion> getGlobalRegions()
    {
        return new HashSet<>(_globalRegions);
    }
    
    @Override
    public Set<IBoundedRegion> getBoundedRegions(Vec3 position)
    {
        Vec3i positionInt = VectorUtils.doubleToInt(position, d -> (int)Math.floor(d));
        return _regionsByPosition.get(positionInt).stream().filter(
                r -> r.contains(position)).collect(Collectors.toSet());
    }
    
    @Override
    public Set<IBoundedRegion> getBoundedRegions(BoundsInt bounds)
    {
        var min = bounds.getMin();
        var max = bounds.getMax();
        
        var delta = _regionsByPosition.delta;
        
        Set<IBoundedRegion> result = new HashSet<>();
        
        for(int z = min.getZ(); z < max.getZ(); z += delta)
        {
            for(int y = min.getY(); y < max.getY(); y += delta)
            {
                for(int x = min.getX(); x < max.getX(); x += delta)
                {
                    Vec3i pos = new Vec3i(x, y, z);
                    var regionsOnPosition = _regionsByPosition.get(pos);
                    regionsOnPosition.forEach(region -> {
                        var closestPosition = region.getBounds().getClosestPosition(Vec3.atLowerCornerOf(pos));
                        if(bounds.contains(closestPosition))
                            result.add(region);
                    });
                }
            }
        }
        return result;
    }
    
    @Override
    public Stream<IRegion> getRegions(Predicate<? super IRegion> predicate)
    {
        return _regionsByName.values().stream().filter(predicate);
    }
    
    @Nullable
    @Override
    public IRegion getRegion(String name)
    {
        return _regionsByName.get(name);
    }
    
    @NotNull
    @Override
    public Iterator<IRegion> iterator()
    {
        return _regionsByName.values().iterator();
    }
    
}
