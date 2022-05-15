package me.doggy.heavyguard.api.flag.node.entity;

import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.flag.node.FlagNode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

public class FlagNodeLivingEntity extends FlagNode
{
    public LivingEntity _livingEntity;
    
    private FlagNodeLivingEntity(LivingEntity livingEntity)
    {
        Objects.requireNonNull(livingEntity);
        _livingEntity = livingEntity;
    }
    
    public static FlagNode create(LivingEntity livingEntity)
    {
        if(livingEntity instanceof Player player)
            return new FlagNodePlayer(player);
        return new FlagNodeLivingEntity(livingEntity);
    }
    
    @Override
    public String getName()
    {
        return "living_entity";
    }
    
    public static ArrayList<String> getPrefix()
    {
        ArrayList<String> result = new ArrayList<>();
        result.add("living_entity");
        result.addAll(FlagNodeEntity.getPrefix());
        return result;
    }
    
    @Override
    public ArrayList<String> getAliases(@Nullable IRegion region)
    {
        ArrayList<String> result = new ArrayList<>();
        String name = FlagNodeEntity.getEntityName(_livingEntity);
        if(name != null)
            result.add(name);
        result.addAll(FlagNode.getClassPathBefore(_livingEntity.getClass(), LivingEntity.class));
        result.addAll(getPrefix());
        return result;
    }
    
}
