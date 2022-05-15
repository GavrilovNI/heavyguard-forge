package me.doggy.heavyguard.flag.node.entity;

import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.flag.FlagPath;
import me.doggy.heavyguard.flag.RegionFlags;
import me.doggy.heavyguard.flag.node.FlagNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class FlagNodeEntity extends FlagNode
{
    private Entity _entity;
    
    private FlagNodeEntity(Entity entity)
    {
        _entity = entity;
    }
    
    public static FlagNode create(Entity entity)
    {
        if(entity instanceof LivingEntity livingEntity)
            return FlagNodeLivingEntity.create(livingEntity);
        return new FlagNodeEntity(entity);
    }
    
    public static ArrayList<String> getPrefix()
    {
        ArrayList<String> result = new ArrayList<>();
        result.add("entity");
        return result;
    }
    
    @Override
    public String getName()
    {
        return "entity";
    }
    
    @Override
    public ArrayList<String> getAliases(@Nullable IRegion region)
    {
        ArrayList<String> result = new ArrayList<>();
        String name = FlagNodeEntity.getEntityName(_entity);
        if(name != null)
            result.add(name);
        result.add(ForgeRegistries.ENTITIES.getKey(_entity.getType()).toString());
        result.add("entity");
        return result;
    }
    
    @Nullable
    public static String getEntityName(Entity entity)
    {
        String name = entity.getName().getContents();
        if(name.isEmpty())
            return null;
        if(name == RegionFlags.ALIAS_ANY)
            return '_' + RegionFlags.ALIAS_ANY;
        return ":name:" + name.replaceAll(" |"+FlagPath.DELIMITER_REGEX, "_");
    }
}
