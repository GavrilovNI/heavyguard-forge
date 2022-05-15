package me.doggy.heavyguard.renderer.shape;


import net.minecraft.world.phys.Vec3;

import java.awt.*;

public abstract class BakedShapeDrawer extends ShapeDrawer
{
    private Vec3[] _vertices;
    
    public BakedShapeDrawer(Color color)
    {
        super(color);
    }
    
    public BakedShapeDrawer(Vec3 offset, Color color)
    {
        super(offset, color);
    }
    
    public final void reBake()
    {
        _vertices = bakeVertices();
    }
    
    protected abstract Vec3[] bakeVertices();
    
    @Override
    protected final Vec3[] getVertices()
    {
        if(_vertices == null)
            reBake();
        return _vertices;
    }
}
