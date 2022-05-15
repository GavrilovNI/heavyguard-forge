package me.doggy.heavyguard.api.region;

import me.doggy.heavyguard.api.math3d.BoundsInt;

public interface IBoundedRegion extends IRegion
{
    BoundsInt getBounds();
}
