package me.doggy.heavyguard.api.region;

public interface IPollutable
{
    void markDirty();
    
    boolean isDirty();
}
