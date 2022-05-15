package me.doggy.heavyguard.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RendererHelper
{
    public static Minecraft getMinecraft(LevelRenderer levelRenderer)
    {
        try
        {
            var field = LevelRenderer.class.getDeclaredField("minecraft");
            field.setAccessible(true);
            return (Minecraft)field.get(levelRenderer);
        }
        catch(NoSuchFieldException|IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
