package me.doggy.heavyguard.region;

import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.event.region.RegionMembersEvent;
import me.doggy.heavyguard.api.region.IRegionMembers;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.util.CompoundTagHelper;
import me.doggy.heavyguard.util.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RegionMembers extends RegionPart implements IRegionMembers
{
    private final Map<UUID, Membership> _members;
    
    public RegionMembers()
    {
        _members = new HashMap<>();
    }
    
    public RegionMembers(Map<UUID, Membership> members)
    {
        _members = members;
    }
    
    @Override
    public int getSize()
    {
        return _members.size();
    }
    
    @Override
    public void setPlayerMembership(UUID playerUuid, Membership membership)
    {
        Membership oldMembership;
        
        if(membership.equals(Membership.Stranger))
            oldMembership = _members.remove(playerUuid);
        else
            oldMembership = _members.put(playerUuid, membership);
        
        if(membership.equals(oldMembership) == false)
        {
            postEventByRegion(region -> new RegionMembersEvent.MembershipUpdated(region, playerUuid, oldMembership, membership));
            markDirty();
        }
    }
    
    @Override
    public Membership getPlayerMembership(UUID playerUuid)
    {
        return _members.getOrDefault(playerUuid, Membership.Stranger);
    }
    
    @Override
    public String toString()
    {
        return getTextBuilder().toString();
    }
    
    @Override
    public TextBuilder getTextBuilder()
    {
        Map<Membership, ChatFormatting> colors = Map.of(
                Membership.Owner, ChatFormatting.GREEN
                ,Membership.Member, ChatFormatting.YELLOW
                ,Membership.Stranger, ChatFormatting.RED
        );
        
        TextBuilder builder = TextBuilder.of();
        for(var memberEntry : _members.entrySet())
        {
            String memberName = PlayerUtils.getNameOrUuid(memberEntry.getKey());
            Membership membership = memberEntry.getValue();
            builder.add(memberName + " : ");
            builder.add(membership.name().toLowerCase(), colors.get(membership));
            builder.startNewLine();
        }
        builder.removeLastLine();
        return builder;
    }
    
    public static void toNbt(IRegionMembers regionMembers, CompoundTag nbt, String key)
    {
        CompoundTagHelper.putMap(nbt, key, regionMembers, (n, k, u) -> n.putUUID(k, u), CompoundTagHelper::putEnum);
    }
    
    public static RegionMembers fromNbt(CompoundTag nbt, String key)
    {
        return new RegionMembers(CompoundTagHelper.getMap(nbt, key, (n, k) -> n.getUUID(k),
                (n, k) -> CompoundTagHelper.getEnum(n, k, Membership.class)));
    }
    
    
    @NotNull
    @Override
    public Iterator<Map.Entry<UUID, Membership>> iterator()
    {
        return _members.entrySet().iterator();
    }
}
