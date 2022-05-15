package me.doggy.heavyguard.region;

import me.doggy.heavyguard.api.event.region.BoundedRegionEvent;
import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IBoundedRegion;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.flag.RegionFlags;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class LevelBoundedRegion extends LevelRegion implements IBoundedRegion
{
    protected BoundsInt _bounds;
    
    public LevelBoundedRegion(String name, ServerLevel world, BoundsInt bounds)
    {
        super(name, world);
        _bounds = bounds;
    }
    
    public LevelBoundedRegion(String name, ServerLevel world, RegionFlags flags, RegionMembers members, BoundsInt bounds)
    {
        super(name, world, flags, members);
        _bounds = bounds;
    }
    
    @Override
    public boolean contains(Vec3 position)
    {
        return _bounds.contains(position);
    }
    
    public void setBounds(BoundsInt bounds)
    {
        var oldBounds = _bounds;
        _bounds = bounds;
        RegionEvent.postEventBy(this, new BoundedRegionEvent.BoundsUpdated(this, oldBounds));
    }
    
    public BoundsInt getBounds()
    {
        return _bounds;
    }
    
    @Override
    public TextBuilder getTextBuilder()
    {
        return super.getTextBuilder().startNewLine(2)
                .add("Bounds: ", ChatFormatting.YELLOW).add(_bounds.toString()).setNextTabLength(0);
    }
}
