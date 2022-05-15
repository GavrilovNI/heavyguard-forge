package me.doggy.heavyguard.renderer.shape;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.doggy.heavyguard.api.math3d.Bounds;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.function.Supplier;

public class CubeLine extends Cube
{
    private boolean _debug;
    
    public CubeLine(Color color, Bounds bounds, boolean debug)
    {
        super(color, bounds);
        _debug = debug;
    }
    
    public CubeLine(Vec3 cameraPosition, Color color, Bounds bounds, boolean debug)
    {
        super(cameraPosition, color, bounds);
        _debug = debug;
    }
    
    @Override
    public Supplier<ShaderInstance> getShader()
    {
        return GameRenderer::getRendertypeLinesShader;
    }
    
    @Override
    public VertexFormat getVertexFormat()
    {
        return DefaultVertexFormat.POSITION_COLOR_NORMAL;
    }
    
    @Override
    public VertexFormat.Mode getDrawMode()
    {
        return _debug ? VertexFormat.Mode.DEBUG_LINES : VertexFormat.Mode.LINES;
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
        
        return new Vec3[]{corners[0], corners[1], corners[1], corners[2], corners[2], corners[3], corners[3], corners[0],
                
                corners[4], corners[5], corners[5], corners[6], corners[6], corners[7], corners[7], corners[4],
                
                corners[0], corners[4], corners[1], corners[5], corners[2], corners[6], corners[3], corners[7]
        };
    }
}
