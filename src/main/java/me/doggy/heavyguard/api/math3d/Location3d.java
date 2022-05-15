package me.doggy.heavyguard.api.math3d;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Location3d<L extends Level> implements Position
{
    private final L _level;
    private final Vec3 _position;
    
    public Location3d(L level, Vec3 position)
    {
        _level = level;
        _position = position;
    }
    
    public L getLevel()
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
    
    @Override
    public String toString()
    {
        return getLevel().toString() + " : " + getPosition().toString();
    }
}
