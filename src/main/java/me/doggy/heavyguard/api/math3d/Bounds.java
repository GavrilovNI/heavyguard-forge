package me.doggy.heavyguard.api.math3d;

import com.mojang.datafixers.util.Function3;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class Bounds
{
    private Vec3 _min;
    private Vec3 _max;
    
    public Bounds(Vec3 a, Vec3 b)
    {
        _min = new Vec3(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
        _max = new Vec3(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
    }
    
    public final Vec3 getMin()
    {
        return _min;
    }
    
    public final Vec3 getMax()
    {
        return _max;
    }
    
    public boolean contains(Vec3 vector)
    {
        if(vector.x < _min.x || vector.y < _min.y || vector.z < _min.z)
            return false;
        if(vector.x >= _max.x || vector.y >= _max.y || vector.z >= _max.z)
            return false;
        return true;
    }
    
    public Vec3 getClosestPositionOnBorder(Vec3 position)
    {
        Function3<Double, Double, Double, Double> getClosest = (min, max, value) -> {
            double distToMin = Math.abs(value - min);
            double distToMax = Math.abs(value - max);
            return distToMin < distToMax ? min : max;
        };
        
        return new Vec3(
                getClosest.apply(_min.x(), _max.x(), position.x()),
                getClosest.apply(_min.y(), _max.y(), position.y()),
                getClosest.apply(_min.z(), _max.z(), position.z())
        );
    }
    public Vec3 getClosestPosition(Vec3 position)
    {
        Function3<Double, Double, Double, Double> getClosest = (min, max, value) -> {
            if(value < min)
                return (double)min;
            if(value > max)
                return (double)max;
            return value;
        };
        
        return new Vec3(
                getClosest.apply(_min.x(), _max.x(), position.x()),
                getClosest.apply(_min.y(), _max.y(), position.y()),
                getClosest.apply(_min.z(), _max.z(), position.z())
        );
    }
    
    public Bounds extend(double value)
    {
        Vec3 extension = new Vec3(1, 1, 1).scale(value);
        return new Bounds(_min.subtract(extension), _max.add(extension));
    }
    
    public Bounds extend(Direction direction, double value)
    {
        Vec3i directionVector = direction.getNormal();
        Vec3 directionVectorD = Vec3.atLowerCornerOf(directionVector);
        Vec3 extension = directionVectorD.scale(value);
        if(direction.getAxisDirection().equals(Direction.AxisDirection.POSITIVE))
            return new Bounds(_min, _max.add(extension));
        else if(direction.getAxisDirection().equals(Direction.AxisDirection.NEGATIVE))
            return new Bounds(_min.add(extension), _max);
        else
            throw new IllegalStateException("Got unsupported axis direction.");
    }
    
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        Bounds bounds = (Bounds)o;
        return _min.equals(bounds._min) && _max.equals(bounds._max);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(_min, _max);
    }
    
    @Override
    public String toString()
    {
        return "Bounds(" + _min.toString() + "; " + _max.toString() + ")";
    }
}
