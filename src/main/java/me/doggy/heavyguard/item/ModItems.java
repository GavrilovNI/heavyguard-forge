package me.doggy.heavyguard.item;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.item.custom.MeasuringTapeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HeavyGuard.MOD_ID);
    
    public static final RegistryObject<Item> MEASURING_TAPE = ITEMS.register("measuring_tape",
            () -> new MeasuringTapeItem(new Item.Properties().tab(ModCreativeModeTab.HEAVY_GUARD)));
    
    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
        HeavyGuard.LOGGER.info("Registering custom mod items.");
    }
}
