package me.doggy.heavyguard.flag.node.entity;

import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.flag.node.FlagNode;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlagNodePlayer extends FlagNode
{
    private static final Membership[] _membershipOrder = new Membership[] { Membership.Owner, Membership.Member, Membership.Stranger };
    
    private final Player _player;
    
    public FlagNodePlayer(Player player)
    {
        Objects.requireNonNull(player);
        _player = player;
    }
    
    public static List<String> getPrefix()
    {
        ArrayList<String> result = new ArrayList<>();
        result.add("player");
        result.addAll(FlagNodeLivingEntity.getPrefix());
        return result;
    }
    
    @Override
    public String getName()
    {
        return "player";
    }
    
    @Override
    public ArrayList<String> getAliases(@Nullable IRegion region)
    {
        ArrayList<String> result = new ArrayList<>();
        
        result.add(FlagNodeEntity.getEntityName(_player));
        
        if(region != null)
        {
            Membership membership = region.getMembers().getPlayerMembership(_player.getUUID());
            int membershipIndex = ArrayUtils.indexOf(_membershipOrder, membership);
            for(int i = membershipIndex; i < _membershipOrder.length; i++)
                result.add(_membershipOrder[i].name().toLowerCase());
        }
        
        result.addAll(getPrefix());
        
        return result;
    }
}
