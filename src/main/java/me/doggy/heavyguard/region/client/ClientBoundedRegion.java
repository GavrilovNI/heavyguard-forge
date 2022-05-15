package me.doggy.heavyguard.region.client;

import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IBoundedRegion;
import me.doggy.heavyguard.util.LevelUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ClientBoundedRegion
{
    private final String _name;
    private final BoundsInt _bounds;
    private final ResourceLocation _levelIdentifier;
    private final Membership _membership;
    
    public ClientBoundedRegion(String name, BoundsInt bounds, ResourceLocation levelIdentifier, Membership membership)
    {
        _name = name;
        _bounds = bounds;
        _levelIdentifier = levelIdentifier;
        _membership = membership;
    }
    
    public ClientBoundedRegion(IBoundedRegion region, ServerPlayer player)
    {
        _name = region.getName();
        _bounds = region.getBounds();
        _levelIdentifier = LevelUtils.getIdentifier(region.getLevel());
        _membership = region.getMembers().getPlayerMembership(player.getUUID());
    }
    
    public String getName()
    {
        return _name;
    }
    
    public BoundsInt getBounds()
    {
        return _bounds;
    }
    
    public Membership getMembership()
    {
        return _membership;
    }
    
    public ResourceLocation getWorldIdentifier()
    {
        return _levelIdentifier;
    }
}
