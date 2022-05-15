package me.doggy.heavyguard.api.math3d;

import com.mojang.datafixers.util.Function3;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class BoundsInt
{
    // lake it says in Vec3i that it's immutable, but it's not, hopefully no one will change it
    private final Vec3i _min;
    private final Vec3i _max;
    
    public BoundsInt(Vec3i a, Vec3i b, boolean inclusive)
    {
        _min = new Vec3i(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        var possibleMax = new Vec3i(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ()));
        if(inclusive)
            _max = possibleMax.offset(new Vec3i(1, 1, 1));
        else
            _max = possibleMax;
    }
    
    public BoundsInt(Vec3i a, Vec3i b)
    {
        this(a, b, false);
    }
    
    public Vec3i getMin()
    {
        return _min;
    }
    
    public Vec3i getMax()
    {
        return _max;
    }
    
    public boolean contains(Vec3 vector)
    {
        if(vector.x() < _min.getX() || vector.y() < _min.getY() || vector.z() < _min.getZ())
            return false;
        if(vector.x() >= _max.getX() || vector.y() >= _max.getY() || vector.z() >= _max.getZ())
            return false;
        return true;
    }
    
    public Vec3i getClosestPositionOnBorder(Vec3 position)
    {
        Function3<Integer, Integer, Double, Integer> getClosest = (min, max, value) -> {
            double distToMin = Math.abs(value - min);
            double distToMax = Math.abs(value - max);
            return distToMin < distToMax ? min : max;
        };
        
        return new Vec3i(
                getClosest.apply(_min.getX(), _max.getX(), position.x()),
                getClosest.apply(_min.getY(), _max.getY(), position.y()),
                getClosest.apply(_min.getZ(), _max.getZ(), position.z())
                );
    }
    public Vec3 getClosestPosition(Vec3 position)
    {
        Function3<Integer, Integer, Double, Double> getClosest = (min, max, value) -> {
            if(value < min)
                return (double)min;
            if(value > max)
                return (double)max;
            return value;
        };
        
        return new Vec3(
                getClosest.apply(_min.getX(), _max.getX(), position.x()),
                getClosest.apply(_min.getY(), _max.getY(), position.y()),
                getClosest.apply(_min.getZ(), _max.getZ(), position.z())
        );
    }
    
    public BoundsInt extend(int value)
    {
        Vec3i extension = new Vec3i(1, 1, 1).multiply(value);
        return new BoundsInt(_min.subtract(extension), _max.offset(extension));
    }
    
    public BoundsInt extend(Direction direction, int value)
    {
        Vec3i extension = direction.getNormal().multiply(value);
        if(direction.getAxisDirection().equals(Direction.AxisDirection.POSITIVE))
            return new BoundsInt(_min, _max.offset(extension));
        else if(direction.getAxisDirection().equals(Direction.AxisDirection.NEGATIVE))
            return new BoundsInt(_min.offset(extension), _max);
        else
            throw new IllegalStateException("Got unsupported axis direction.");
    }
    
    public Bounds toDouble()
    {
        return new Bounds(Vec3.atLowerCornerOf(_min), Vec3.atLowerCornerOf(_max));
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        BoundsInt boundsInt = (BoundsInt)o;
        return _min.equals(boundsInt._min) && _max.equals(boundsInt._max);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(_min, _max);
    }
    
    @Override
    public String toString()
    {
        return "Bounds(" + _min.toShortString() + "; " + _max.toShortString() + ")";
    }
}
