package me.doggy.heavyguard.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.doggy.heavyguard.api.math3d.Bounds;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.renderer.shape.CubeLine;
import me.doggy.heavyguard.renderer.shape.CubeQuad;
import me.doggy.heavyguard.renderer.shape.ShapeDrawer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;

import java.awt.*;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class BoundsDrawer
{
    public static void draw(RenderLevelLastEvent event, Vec3 cameraPosition, BoundsInt boundsInt, Color solidColor, Color wireColor)
    {
        Objects.requireNonNull(event);
        Objects.requireNonNull(cameraPosition);
        Objects.requireNonNull(boundsInt);
        Objects.requireNonNull(solidColor);
        Objects.requireNonNull(wireColor);
        
        Bounds bounds = boundsInt.toDouble();
        
        var extension = bounds.contains(cameraPosition) ? -0.001 : 0.001;
        bounds = bounds.extend(extension);
        
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().mulPoseMatrix(event.getPoseStack().last().pose());
        RenderSystem.applyModelViewMatrix();
        
        RenderSystem.disableCull(); // disable to see from the other side
        RenderSystem.enableBlend(); // enable transparent
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        
        ShapeDrawer wireCubeDrawer = new CubeLine(cameraPosition, wireColor, bounds, true);
        ShapeDrawer solidCubeDrawer = new CubeQuad(cameraPosition, solidColor, bounds);
        
        final ShaderInstance oldShader = RenderSystem.getShader();
        
        ShapeDrawer.Draw(wireCubeDrawer);
        ShapeDrawer.Draw(solidCubeDrawer);
        
        RenderSystem.setShader(() -> oldShader);
    
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
