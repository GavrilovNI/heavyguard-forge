package me.doggy.heavyguard.api.interaction;

import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsProvider;
import me.doggy.heavyguard.api.utils.RegionUtils;
import net.minecraft.server.level.ServerLevel;

public abstract class LocatedInteraction implements Interaction
{
    private final Location3d<ServerLevel> _location;
    
    public LocatedInteraction(Location3d<ServerLevel> location)
    {
        _location = location;
    }
    
    public Location3d<ServerLevel> getLocation()
    {
        return _location;
    }
    
    
    public abstract InteractionResult test(IRegion region);
    
    @Override
    public final InteractionResult test(IRegionsProvider regionsProvider)
    {
        var regions = regionsProvider
                .getRegions(_location.getLevel())
                .getRegions(_location.getPosition());
        
        if(regions.isEmpty())
            return InteractionResult.pass();
        
        return test(RegionUtils.getMostPrioritizedRegion(regions));
    }
}
