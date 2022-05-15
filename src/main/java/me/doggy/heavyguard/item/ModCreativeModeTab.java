package me.doggy.heavyguard.item;

import me.doggy.heavyguard.HeavyGuard;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab
{
    public static final CreativeModeTab HEAVY_GUARD = new CreativeModeTab(HeavyGuard.MOD_ID)
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(ModItems.MEASURING_TAPE.get());
        }
    };
}
