package me.doggy.heavyguard.api.interaction;

import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsProvider;
import me.doggy.heavyguard.api.utils.RegionUtils;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.Set;

public abstract class DeltaInteraction implements Interaction
{
    private final Location3d<ServerLevel> _locationA;
    private final Location3d<ServerLevel> _locationB;
    
    public DeltaInteraction(Location3d<ServerLevel> locationA, Location3d<ServerLevel> locationB)
    {
        Objects.requireNonNull(locationA);
        Objects.requireNonNull(locationB);
        _locationA = locationA;
        _locationB = locationB;
    }
    
    public Location3d<ServerLevel> getLocationA()
    {
        return _locationA;
    }
    public Location3d<ServerLevel> getLocationB()
    {
        return _locationB;
    }
    
    
    public abstract InteractionResult test(Set<IRegion> regionsA, Set<IRegion> regionsB);
    
    @Override
    public final InteractionResult test(IRegionsProvider regionsProvider)
    {
        var regionsA = regionsProvider
                .getRegions(_locationA.getLevel())
                .getRegions(_locationA.getPosition());
        var regionsB = regionsProvider
                .getRegions(_locationB.getLevel())
                .getRegions(_locationB.getPosition());
        
        if(regionsA.isEmpty() && regionsB.isEmpty())
            return InteractionResult.pass();
        
        return test(regionsA, regionsB);
    }
}