package me.doggy.heavyguard.api.event.region;

import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.region.IRegion;

import java.util.UUID;

public abstract class RegionMembersEvent extends RegionEvent
{
    private final UUID _memberUuid;
    
    public RegionMembersEvent(IRegion region, UUID memberUuid)
    {
        super(region);
        _memberUuid = memberUuid;
    }
    
    public UUID getMemberUuid()
    {
        return _memberUuid;
    }
    
    public static class MembershipUpdated extends RegionMembersEvent
    {
        private final Membership _oldMembership;
        private final Membership _newMembership;
        
        public MembershipUpdated(IRegion region, UUID memberUuid, Membership oldMembership, Membership newMembership)
        {
            super(region, memberUuid);
            _oldMembership = oldMembership;
            _newMembership = newMembership;
        }
        
        public Membership getNewMembership()
        {
            return _newMembership;
        }
    
        public Membership getOldMembership()
        {
            return _oldMembership;
        }
    }
}
