package me.doggy.heavyguard.api.flag.node;

import me.doggy.heavyguard.api.region.IRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;

public class FlagNodeBlock extends FlagNode
{
    public static final String MAIN_ALIAS = "block";
    
    private final Block _block;
    
    public FlagNodeBlock(Block block)
    {
        Objects.requireNonNull(block);
        _block = block;
    }
    
    
    @Override
    public String getName()
    {
        return "block";
    }
    
    @Override
    public ArrayList<String> getAliases(@Nullable IRegion region)
    {
        ArrayList<String> result = new ArrayList<>();
        
        result.add(ForgeRegistries.BLOCKS.getKey(_block).toString());
        if(_block instanceof EntityBlock)
        {
            result.addAll(FlagNode.getClassPath(((EntityBlock)_block).getClass(), EntityBlock.class));
            result.add(FlagNode.classToFlagNodeName(EntityBlock.class));
        }
        else
        {
            result.addAll(FlagNode.getClassPath(_block.getClass(), Block.class));
        }
        result.add(MAIN_ALIAS);
        
        return result;
    }
}
