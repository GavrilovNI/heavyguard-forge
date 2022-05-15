package me.doggy.heavyguard.renderer;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.region.client.ClientBoundedRegion;
import me.doggy.heavyguard.region.client.ClientRegions;
import me.doggy.heavyguard.util.RendererHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = HeavyGuard.MOD_ID, value = {Dist.CLIENT})
public class RegionsDrawer
{
    public static void draw(RenderLevelLastEvent event, ClientBoundedRegion region, Color solidColor, Color wireColor)
    {
        Objects.requireNonNull(event);
        Objects.requireNonNull(solidColor);
        Objects.requireNonNull(wireColor);
        
        var minecraft = RendererHelper.getMinecraft(event.getLevelRenderer());
        
        var cameraPosition = minecraft.gameRenderer.getMainCamera().getPosition();
        BoundsDrawer.draw(event, cameraPosition, region.getBounds(), solidColor, wireColor);
    }
    
    @SubscribeEvent
    public static void onRender(RenderLevelLastEvent event)
    {
        Map<Membership, Color> solidColors = Map.of(
                Membership.Owner, new Color(0, 255, 0, 100),
                Membership.Member, new Color(255, 255, 0, 100),
                Membership.Stranger, new Color(255, 0, 0, 100)
        );
        Color wireColor = new Color(0, 255, 255, 255);
    
        for(var region : ClientRegions.instance())
            draw(event, region, solidColors.get(region.getMembership()), wireColor);
    }
}
