package me.doggy.heavyguard.api.flag.node;

import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.math3d.Location3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FlagNodeWorldBlock extends FlagNode
{
    private final Location3i _location;
    private final List<String> _additionalAliases;
    
    public FlagNodeWorldBlock(Location3i location, String... additionalAliases)
    {
        Objects.requireNonNull(location);
        Objects.requireNonNull(additionalAliases);
        _location = location;
        _additionalAliases = Arrays.stream(additionalAliases).toList();
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
        Level level = _location.getLevel();
        BlockPos blockPos = _location.getPosition();
    
        var block = level.getBlockState(blockPos).getBlock();
    
        result.add(ForgeRegistries.BLOCKS.getKey(block).toString());
        
        if(block instanceof EntityBlock)
        {
            result.addAll(FlagNode.getClassPath(((EntityBlock)block).getClass(), EntityBlock.class));
            
            var blockEntity = level.getBlockEntity(blockPos);
            if(blockEntity instanceof Clearable)
            {
                result.add("clearable_block"); // jukebox
            }
            if(blockEntity instanceof ICapabilityProvider)
            {
                result.add("inventory_block"); // chest, furnace...
            }
            result.add(FlagNode.classToFlagNodeName(EntityBlock.class));
        }
        else
        {
            result.addAll(FlagNode.getClassPath(block.getClass(), Block.class));
        }
        
        result.addAll(_additionalAliases);
        result.add(FlagNodeBlock.MAIN_ALIAS);
        return result;
    }
}
