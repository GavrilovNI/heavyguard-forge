package me.doggy.heavyguard.math3d;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import net.minecraft.core.Vec3i;

import java.util.Collection;

public class MultiMap3D<T>
{
    public final int delta;
    private final Multimap<Vec3i, T> _map = ArrayListMultimap.create();
    
    public MultiMap3D(int delta)
    {
        if(delta < 1)
            throw new IllegalArgumentException("delta must be more than 0");
        this.delta = delta;
    }
    
    private Vec3i getKey(Vec3i position)
    {
        return new Vec3i(position.getX() / delta, position.getY() / delta, position.getZ() / delta);
    }
    
    public Collection<T> get(Vec3i position)
    {
        return _map.get(getKey(position));
    }
    
    public void put(Vec3i position, T value)
    {
        _map.put(getKey(position), value);
    }
    
    public void put(BoundsInt bounds, T value)
    {
        var min = bounds.getMin();
        var max = bounds.getMax();
        
        for(int z = min.getZ(); z < max.getZ(); z += delta)
            for(int y = min.getY(); y < max.getY(); y += delta)
                for(int x = min.getX(); x < max.getX(); x += delta)
                    put(new Vec3i(x, y, z), value);
    }
    
    public void remove(Vec3i position, T value)
    {
        _map.remove(getKey(position), value);
    }
    
    public void remove(BoundsInt bounds, T value)
    {
        var min = bounds.getMin();
        var max = bounds.getMax();
        
        for(int z = min.getZ(); z < max.getZ(); z += delta)
            for(int y = min.getY(); y < max.getY(); y += delta)
                for(int x = min.getX(); x < max.getX(); x += delta)
                    remove(new Vec3i(x, y, z), value);
    }
    
    public void clear()
    {
        _map.clear();
    }
}
