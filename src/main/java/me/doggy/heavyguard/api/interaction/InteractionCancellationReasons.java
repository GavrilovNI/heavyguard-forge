package me.doggy.heavyguard.api.interaction;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.flag.FlagTypePath;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class InteractionCancellationReasons
{
    public static final TranslatableComponent youDontHaveRegionPermission(FlagTypePath flag)
    {
        return new TranslatableComponent(HeavyGuard.MOD_ID + ".you-dont-have-region-permission", flag.toString());
    }
    
    public static final TranslatableComponent youOrPassengerOrVehicleDontHaveRegionPermission(FlagTypePath flag, Entity entity)
    {
        return new TranslatableComponent(HeavyGuard.MOD_ID + ".you-passenger-vehicle-dont-have-region-permission", flag.toString(), entity.getName());
    }
}
