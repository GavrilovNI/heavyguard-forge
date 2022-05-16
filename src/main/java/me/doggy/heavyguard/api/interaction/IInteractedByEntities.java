package me.doggy.heavyguard.api.interaction;

import net.minecraft.world.entity.Entity;

import java.util.Set;

public interface IInteractedByEntities
{
    Set<Entity> getInteractors();
}
