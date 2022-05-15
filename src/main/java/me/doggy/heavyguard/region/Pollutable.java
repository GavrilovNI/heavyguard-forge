package me.doggy.heavyguard.region;

import me.doggy.heavyguard.api.region.IPollutable;

public abstract class Pollutable implements IPollutable
{
    private boolean _isDirty = true;
    
    public void markDirty()
    {
        _isDirty = true;
    }
    
    public boolean isDirty()
    {
        return _isDirty;
    }
    
    public void markClean()
    {
        _isDirty = false;
    }
}
