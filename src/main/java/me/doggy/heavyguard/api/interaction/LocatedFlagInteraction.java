package me.doggy.heavyguard.api.interaction;

import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.api.region.IRegion;
import net.minecraft.server.level.ServerLevel;

public abstract class LocatedFlagInteraction extends LocatedInteraction
{
    public LocatedFlagInteraction(Location3d<ServerLevel> location)
    {
        super(location);
    }
    
    public abstract FlagTypePath getFlag();
    
    @Override
    public InteractionResult test(IRegion region)
    {
        var flag = getFlag();
        return InteractionResult.of(region.canInteract(flag) == false, InteractionCancellationReasons.youDontHaveRegionPermission(flag));
    }
}
