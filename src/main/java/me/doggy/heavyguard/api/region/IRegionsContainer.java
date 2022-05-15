package me.doggy.heavyguard.api.region;

import me.doggy.heavyguard.api.math3d.BoundsInt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface IRegionsContainer extends Iterable<IRegion>
{
    IEventBus getEventBus();
    
    ServerLevel getLevel();
    
    void addRegion(IRegion region);
    
    void removeRegion(IRegion region);
    
    void removeRegion(String name);
    
    void clear();
    
    boolean hasRegion(IRegion region);
    
    boolean hasRegion(String name);
    
    default Set<IRegion> getRegions(Vec3 position)
    {
        return Stream.of(getGlobalRegions(), getBoundedRegions(position)).flatMap(Collection::stream).collect(Collectors.toSet());
    }
    default Set<IRegion> getRegions(BoundsInt bounds)
    {
        return Stream.of(getGlobalRegions(), getBoundedRegions(bounds)).flatMap(Collection::stream).collect(Collectors.toSet());
    }
    
    Set<IRegion> getGlobalRegions();
    Set<IBoundedRegion> getBoundedRegions(Vec3 position);
    Set<IBoundedRegion> getBoundedRegions(BoundsInt bounds);
    
    Stream<IRegion> getRegions(Predicate<? super IRegion> predicate);
    
    default Collection<IRegion> getRegions()
    {
        return getRegions(r -> true).collect(Collectors.toSet());
    }
    
    @Nullable
    IRegion getRegion(String name);
}
