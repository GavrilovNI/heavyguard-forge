package me.doggy.heavyguard.math3d;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Location3d implements Position
{
    private final Level _level;
    private final Vec3 _position;
    
    public Location3d(Level world, Vec3 position)
    {
        _level = world;
        _position = position;
    }
    
    public Level getLevel()
    {
        return _level;
    }
    
    public Vec3 getPosition()
    {
        return _position;
    }
    
    @Override
    public double x()
    {
        return _position.x;
    }
    
    @Override
    public double y()
    {
        return _position.y;
    }
    
    @Override
    public double z()
    {
        return _position.z;
    }
}
