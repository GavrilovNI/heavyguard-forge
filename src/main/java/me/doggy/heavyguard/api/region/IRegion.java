package me.doggy.heavyguard.api.region;

import me.doggy.heavyguard.api.flag.IRegionFlags;
import me.doggy.heavyguard.api.utils.ITextable;
import me.doggy.heavyguard.api.flag.FlagTypePath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;

public interface IRegion extends IPollutable, ITextable
{
    IEventBus getEventBus();
    
    ServerLevel getLevel();
    
    String getName();
    
    IRegionMembers getMembers();
    
    IRegionFlags getFlags();
    
    int getPriority();
    void setPriority(int priority);
    
    boolean contains(Vec3 position);
    
    boolean canInteract(FlagTypePath path);
}
