package me.doggy.heavyguard.api.interaction;

import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.Set;

public interface IInteractedByEntity extends IInteractedByEntities
{
    Entity getInteractor();
    @Override
    default Set<Entity> getInteractors()
    {
        var result = new HashSet<Entity>();
        result.add(getInteractor());
        return result;
    }
}
