package me.doggy.heavyguard.renderer.shape;

import me.doggy.heavyguard.api.math3d.Bounds;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public abstract class Cube extends BakedShapeDrawer
{
    private Bounds _bounds;
    
    public Cube(Color color, Bounds bounds)
    {
        super(color);
        SetBounds(bounds);
    }
    
    public Cube(Vec3 cameraPosition, Color color, Bounds bounds)
    {
        super(cameraPosition, color);
        SetBounds(bounds);
    }
    
    public void SetBounds(Bounds bounds)
    {
        if(bounds == null)
            throw new NullPointerException("bounds is null");
        _bounds = bounds;
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
    protected final Vec3[] getCorners()
    {
        Vec3 min = _bounds.getMin();
        Vec3 max = _bounds.getMax();
        
        return new Vec3[]{
                min,
                new Vec3(max.x, min.y, min.z),
                new Vec3(max.x, max.y, min.z),
                new Vec3(min.x, max.y, min.z),
                new Vec3(min.x, min.y, max.z),
                new Vec3(max.x, min.y, max.z),
                max,
                new Vec3(min.x, max.y, max.z)
        };
    }
}
