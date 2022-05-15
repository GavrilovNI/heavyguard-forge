package me.doggy.heavyguard.renderer.shape;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.function.Supplier;


public abstract class ShapeDrawer
{
    protected Vec3 _cameraPosition;
    protected Color _color;
    
    public boolean ConnectLineStrip = false;
    
    public ShapeDrawer(Vec3 cameraPosition, Color color)
    {
        setCameraPosition(cameraPosition);
        setColor(color);
    }
    
    public ShapeDrawer(Color color)
    {
        this(new Vec3(0, 0, 0), color);
    }
    
    public void setCameraPosition(Vec3 cameraPosition)
    {
        if(cameraPosition == null)
            throw new NullPointerException("cameraPosition is null");
        _cameraPosition = cameraPosition;
    }
    
    public void setColor(Color color)
    {
        if(color == null)
            throw new NullPointerException("color is null");
        _color = color;
    }
    
    public Color getColor()
    {
        return _color;
    }
    
    protected abstract Vec3[] getVertices();
    
    public abstract Supplier<ShaderInstance> getShader();
    
    public abstract VertexFormat getVertexFormat();
    
    public abstract VertexFormat.Mode getDrawMode();
    
    private static void addVertex(BufferBuilder bufferBuilder, Vec3 pos, Color color, Vec3 normal)
    {
        bufferBuilder.vertex(pos.x, pos.y, pos.z).color(color.getRed(), color.getGreen(), color.getBlue(),
                color.getAlpha()).normal((float)normal.x, (float)normal.y, (float)normal.z).endVertex();
    }
    
    private static void addVertex(BufferBuilder bufferBuilder, Vec3 pos, Color color)
    {
        bufferBuilder.vertex(pos.x, pos.y, pos.z).color(color.getRed(), color.getGreen(), color.getBlue(),
                color.getAlpha()).endVertex();
    }
    
    public void draw(BufferBuilder bufferBuilder)
    {
        var vertices = getVertices();
        VertexFormat.Mode drawMode = getDrawMode();
        if(drawMode == VertexFormat.Mode.QUADS)
        {
            for(int i = 0; i < vertices.length; i++)
            {
                Vec3 pos = vertices[i].subtract(_cameraPosition);
                bufferBuilder.vertex(pos.x, pos.y, pos.z).color(_color.getRed(), _color.getGreen(), _color.getBlue(),
                        _color.getAlpha()).endVertex();
            }
        }
        else if(drawMode == VertexFormat.Mode.LINES || drawMode == VertexFormat.Mode.DEBUG_LINES)
        {
            boolean isDebug = drawMode == VertexFormat.Mode.DEBUG_LINES;
            int length = vertices.length / 2 * 2;
            for(int i = 0; i < length; i += 2)
            {
                Vec3 pos = vertices[i].subtract(_cameraPosition);
                Vec3 nextPos = vertices[i + 1].subtract(_cameraPosition);
                if(pos.equals(nextPos))
                    continue;
                
                Vec3 normal = isDebug ? new Vec3(0, 0, 0) : pos.subtract(nextPos).normalize();
                
                addVertex(bufferBuilder, pos, _color, normal);
                addVertex(bufferBuilder, nextPos, _color, normal);
            }
        }
        else if(drawMode == VertexFormat.Mode.LINE_STRIP || drawMode == VertexFormat.Mode.DEBUG_LINE_STRIP)
        {
            if(vertices.length == 1)
                return;
            
            boolean isDebug = drawMode == VertexFormat.Mode.DEBUG_LINE_STRIP;
            int length = ConnectLineStrip ? vertices.length + 1 : vertices.length;
            
            Vec3 currPos = vertices[0].subtract(_cameraPosition);
            Vec3 prevPos = ConnectLineStrip ? vertices[vertices.length - 1].subtract(_cameraPosition) : currPos;
            
            for(int i = 0; i < length; i++)
            {
                boolean isLastAndNotConnect = ConnectLineStrip == false && i == length - 1;
                Vec3 nextPos = isLastAndNotConnect ? currPos : vertices[(i + 1) % vertices.length].subtract(
                        _cameraPosition);
                
                Vec3 normal;
                if(isDebug)
                {
                    //no idea why it's require normal. line doesn't change when normal changes
                    normal = Vec3.ZERO;
                }
                else
                {
                    var deltaToPrev = prevPos.subtract(currPos).normalize();
                    var deltaToNext = nextPos.subtract(currPos).normalize();
                    
                    normal = deltaToNext.add(deltaToPrev).normalize();
                }
                
                addVertex(bufferBuilder, currPos, _color, normal);
                
                prevPos = currPos;
                currPos = nextPos;
            }
        }
        else
        {
            throw new IllegalStateException("Unknown drawMode");
        }
    }
    
    public static void Draw(ShapeDrawer drawer)
    {
        Tesselator tesselator = Tesselator.getInstance();
        
        var bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(drawer.getDrawMode(), drawer.getVertexFormat());
        drawer.draw(bufferBuilder);
        RenderSystem.setShader(drawer.getShader());
        tesselator.end();
    }
}
