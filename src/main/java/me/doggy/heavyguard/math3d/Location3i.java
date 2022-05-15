package me.doggy.heavyguard.math3d;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

public class Location3i implements Position
{
    private final Level _level;
    private final BlockPos _position;
    
    public Location3i(Level level, Vec3i position)
    {
        _level = level;
        _position = new BlockPos(position);
    }
    
    public Level getLevel()
    {
        return _level;
    }
    
    public BlockPos getPosition()
    {
        return _position;
    }
    
    @Override
    public double x()
    {
        return _position.getX();
    }
    
    @Override
    public double y()
    {
        return _position.getY();
    }
    
    @Override
    public double z()
    {
        return _position.getZ();
    }
}
