package me.doggy.heavyguard.api.flag.node;

import me.doggy.heavyguard.api.region.IRegion;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;

public class FlagNodeFluid extends FlagNode
{
    private final Fluid _fluid;
    
    public FlagNodeFluid(Fluid fluid)
    {
        Objects.requireNonNull(fluid);
        _fluid = fluid;
    }
    
    
    @Override
    public String getName()
    {
        return "fluid";
    }
    
    @Override
    public ArrayList<String> getAliases(@Nullable IRegion region)
    {
        ArrayList<String> result = new ArrayList<>();
        
        result.add(ForgeRegistries.FLUIDS.getKey(_fluid).toString());
        result.addAll(FlagNode.getClassPathBefore(_fluid.getClass(), Fluid.class));
        result.add("fluid");
        
        return result;
    }
}