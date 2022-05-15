package me.doggy.heavyguard.api.math3d;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Location3i<L extends Level> implements Position
{
    private final L _level;
    private final BlockPos _position;
    
    public Location3i(L level, Vec3i position)
    {
        _level = level;
        _position = new BlockPos(position);
    }
    
    public Location3d<L> to3d()
    {
        return new Location3d<L>(_level, Vec3.atLowerCornerOf(_position));
    }
    
    public L getLevel()
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
    
    @Override
    public String toString()
    {
        return getLevel().toString() + " : " + getPosition().toString();
    }
}
