package me.doggy.heavyguard.api.flag.node;

import me.doggy.heavyguard.api.region.IRegion;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class FlagNodeItem extends FlagNode
{
    private final Item _item;
    
    public FlagNodeItem(Item item)
    {
        _item = item;
    }
    
    @Override
    public String getName()
    {
        return "item";
    }
    
    @Override
    public ArrayList<String> getAliases(@Nullable IRegion region)
    {
        ArrayList<String> result = new ArrayList<>();
        result.add(ForgeRegistries.ITEMS.getKey(_item).toString());
        result.addAll(FlagNode.getClassPathBefore(_item.getClass(), Item.class));
        result.add("item");
        return result;
    }
}
