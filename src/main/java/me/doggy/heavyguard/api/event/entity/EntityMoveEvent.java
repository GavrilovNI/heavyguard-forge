package me.doggy.heavyguard.api.event.entity;

import me.doggy.heavyguard.api.math3d.Location3d;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Objects;

public abstract class EntityMoveEvent extends EntityEvent
{
    private final Location3d _oldLocation;
    private final Location3d _newLocation;
    private final MoveType _moveType;
    
    public EntityMoveEvent(Entity entity, Location3d oldLocation, Location3d newLocation, MoveType moveType)
    {
        super(entity);
        Objects.requireNonNull(oldLocation);
        Objects.requireNonNull(newLocation);
        Objects.requireNonNull(moveType);
        if(oldLocation.getLevel().isClientSide() != newLocation.getLevel().isClientSide())
            throw new IllegalArgumentException("Both locations must be client or not client side.");
        _oldLocation = oldLocation;
        _newLocation = newLocation;
        _moveType = moveType;
    }
    
    public Location3d getOldLocation()
    {
        return _oldLocation;
    }
    
    public Location3d getNewLocation()
    {
        return _newLocation;
    }
    
    public MoveType getMoveType()
    {
        return _moveType;
    }
    
    
    @Cancelable
    public static class CanMove extends EntityMoveEvent
    {
        public CanMove(Entity entity, Location3d oldLocation, Location3d newLocation, MoveType moveType)
        {
            super(entity, oldLocation, newLocation, moveType);
        }
    }
    
    public static class Moved extends EntityMoveEvent
    {
        public Moved(Entity entity, Location3d oldLocation, Location3d newLocation, MoveType moveType)
        {
            super(entity, oldLocation, newLocation, moveType);
        }
    }
}
