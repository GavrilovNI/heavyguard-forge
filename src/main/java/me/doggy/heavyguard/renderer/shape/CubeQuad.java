package me.doggy.heavyguard.renderer.shape;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.doggy.heavyguard.api.math3d.Bounds;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.function.Supplier;

public class CubeQuad extends Cube
{
    public CubeQuad(Color color, Bounds bounds)
    {
        super(color, bounds);
    }
    
    public CubeQuad(Vec3 cameraPosition, Color color, Bounds bounds)
    {
        super(cameraPosition, color, bounds);
    }
    
    @Override
    public Supplier<ShaderInstance> getShader()
    {
        return GameRenderer::getPositionColorShader;
    }
    
    @Override
    public VertexFormat getVertexFormat()
    {
        return DefaultVertexFormat.POSITION_COLOR;
    }
    
    @Override
    public VertexFormat.Mode getDrawMode()
    {
        return VertexFormat.Mode.QUADS;
    }
    
    //
    //  6_____7
    //  |\   |\
    //  |2\____\3
    // 5\-|--4 |
    //   \|____|
    //    1    0
    //
    //     z  y
    //      \ |
    //    x__\|
    //
    @Override
    protected Vec3[] bakeVertices()
    {
        Vec3[] corners = getCorners();
        
        return new Vec3[]{
                corners[0], corners[1], corners[5], corners[4], // bottom
                corners[7], corners[6], corners[2], corners[3], // top
                corners[3], corners[2], corners[1], corners[0], // front
                corners[4], corners[5], corners[6], corners[7], // back
                corners[2], corners[6], corners[5], corners[1], // left
                corners[4], corners[7], corners[3], corners[0], // right
        };
    }
}
