package me.doggy.heavyguard.api.interaction;

import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.api.math3d.Location3d;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

public abstract class Interaction extends Event
{
    private final Location3d<ServerLevel> _location;
    
    public Interaction(Location3d<ServerLevel> location)
    {
        _location = location;
    }
    
    public Location3d<ServerLevel> getLocation()
    {
        return _location;
    }
    
    @Override
    public final boolean isCancelable()
    {
        return true;
    }
    
    public abstract FlagTypePath getFlagPath();
}
