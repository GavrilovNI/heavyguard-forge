package me.doggy.heavyguard.api.interaction;

import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.event.entity.MoveType;
import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.api.flag.node.FlagNodeLiteral;
import me.doggy.heavyguard.api.flag.node.entity.FlagNodeEntity;
import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.utils.RegionUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class EntityEnterAndLeaveInteraction extends DeltaInteraction implements IInteractedByEntities
{
    private final Entity _interactor;
    private final MoveType _moveType;
    
    public EntityEnterAndLeaveInteraction(Entity interactor, Location3d<ServerLevel> locationA, Location3d<ServerLevel> locationB, MoveType moveType)
    {
        super(locationA, locationB);
        Objects.requireNonNull(interactor);
        Objects.requireNonNull(moveType);
        _interactor = interactor;
        _moveType = moveType;
    }
    
    public static EntityEnterAndLeaveInteraction create(EntityMoveEvent event)
    {
        if(event.getNewLocation().getLevel() instanceof ServerLevel)
        {
            return new EntityEnterAndLeaveInteraction(event.getEntity(), event.getOldLocation(), event.getNewLocation(), event.getMoveType());
        }
        return null;
    }
    
    private InteractionResult test(Set<Entity> entities, FlagTypePath subPath, IRegion region)
    {
        for(var entity : entities)
        {
            FlagTypePath flagPath = FlagTypePath.of(
                    FlagNodeEntity.create(entity)
            ).add(subPath);
    
            boolean canInteract = region.canInteract(flagPath);
            if(canInteract == false)
                return InteractionResult.cancel(InteractionCancellationReasons.youOrPassengerOrVehicleDontHaveRegionPermission(flagPath, entity));
        }
    
        return InteractionResult.pass();
    }
    
    public InteractionResult test(Set<Entity> entities, FlagTypePath subPath, Set<IRegion> regions)
    {
        if(regions.isEmpty())
            return InteractionResult.pass();
    
        var region = RegionUtils.getMostPrioritizedRegion(regions);
        return test(entities, subPath, region);
    }
    
    @Override
    public InteractionResult test(Set<IRegion> regionsA, Set<IRegion> regionsB)
    {
        String moveTypeStr = _moveType.name().toLowerCase();
        var entities = getInteractors();
        
        //leave
        var leavingRegions = new HashSet<>(regionsA);
        leavingRegions.removeAll(regionsB);
        FlagTypePath leavingPath = FlagTypePath.of(
                new FlagNodeLiteral("leave"),
                new FlagNodeLiteral(moveTypeStr)
        );
        var result = test(entities, leavingPath, leavingRegions);
        
        //enter
        if(result.isCancelled() == false)
        {
            var enteringRegions = new HashSet<>(regionsB);
            enteringRegions.removeAll(regionsA);
            FlagTypePath enteringPath = FlagTypePath.of(
                    new FlagNodeLiteral("enter"),
                    new FlagNodeLiteral(moveTypeStr)
            );
            result = test(entities, enteringPath, enteringRegions);
        }
        
        return result;
    }
    
    public MoveType getMoveType()
    {
        return _moveType;
    }
    
    @Override
    public Set<Entity> getInteractors()
    {
        var result = new HashSet<Entity>();
    
        Consumer<Entity> addItAndPassengers = new Consumer<Entity>()
        {
            @Override
            public void accept(Entity entity)
            {
                result.add(entity);
                for(var passanger : entity.getPassengers())
                    accept(passanger);
            }
        };
        addItAndPassengers.accept(_interactor);
        return result;
    }
}
