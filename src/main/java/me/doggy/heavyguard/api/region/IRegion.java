package me.doggy.heavyguard.api.region;

import me.doggy.heavyguard.api.utils.ITextable;
import me.doggy.heavyguard.flag.FlagTypePath;
import me.doggy.heavyguard.flag.RegionFlags;
import me.doggy.heavyguard.region.RegionMembers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;

public interface IRegion extends IPollutable, ITextable
{
    IEventBus getEventBus();
    
    ServerLevel getLevel();
    
    String getName();
    
    RegionMembers getMembers();
    
    RegionFlags getFlags();
    
    boolean contains(Vec3 position);
    
    boolean canInteract(FlagTypePath path);
}
