package me.doggy.heavyguard.renderer;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.item.ModItems;
import me.doggy.heavyguard.item.custom.MeasuringTapeItem;
import me.doggy.heavyguard.util.RendererHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = HeavyGuard.MOD_ID, value = {Dist.CLIENT})
public class MeasuringTapeDrawer
{
    @Nullable
    private static ItemStack findMeasuringTape(Entity entity)
    {
        var items = entity.getHandSlots();
        
        for(var item : items)
        {
            if(item.getItem() == ModItems.MEASURING_TAPE.get())
                return item;
        }
        return null;
    }
    
    public static void draw(RenderLevelLastEvent event, ItemStack measuringTape, Color solidColor, Color wireColor)
    {
        Objects.requireNonNull(event);
        Objects.requireNonNull(measuringTape);
        Objects.requireNonNull(solidColor);
        Objects.requireNonNull(wireColor);
        
        Level level = MeasuringTapeItem.getClientLevel(measuringTape);
        if(level == null)
            return;
        
        var minecraft = RendererHelper.getMinecraft(event.getLevelRenderer());
        
        if(level == minecraft.level == false)
            return;
        BoundsInt boundsInt = MeasuringTapeItem.getBounds(measuringTape);
        if(boundsInt == null)
            return;
        
        var cameraPosition = minecraft.gameRenderer.getMainCamera().getPosition();
        
        BoundsDrawer.draw(event, cameraPosition, boundsInt, solidColor, wireColor);
    }
    
    @SubscribeEvent
    public static void onRender(RenderLevelLastEvent event)
    {
        var minecraft = RendererHelper.getMinecraft(event.getLevelRenderer());
        var player = minecraft.player;
        if(player == null)
            return;
        var measuringTape = findMeasuringTape(player);
        if(measuringTape == null)
            return;
    
        Color solidColor = new Color(255, 255, 255, 100);
        Color wireColor = new Color(0, 0, 0, 255);
    
        draw(event, measuringTape, solidColor, wireColor);
    }
}
