package me.doggy.heavyguard.api.region;

import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.utils.ITextable;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public interface IRegionMembers extends Iterable<Map.Entry<UUID, Membership>>, ITextable
{
    void setPlayerMembership(UUID playerUuid, Membership membership);
    
    Membership getPlayerMembership(UUID playerUuid);
    
    int getSize();
    
    @NotNull Iterator<Map.Entry<UUID, Membership>> iterator();
}
