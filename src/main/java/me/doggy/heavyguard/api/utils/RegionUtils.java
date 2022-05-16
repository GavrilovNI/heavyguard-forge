package me.doggy.heavyguard.api.utils;

import me.doggy.heavyguard.api.region.IRegion;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public class RegionUtils
{
    @Nullable
    public static IRegion getMostPrioritizedRegion(Collection<IRegion> regions)
    {
        Objects.requireNonNull(regions);
        if(regions.isEmpty())
            return null;
        
        var sorted = new ArrayList<>(regions);
        sorted.sort(Comparator.comparingInt(IRegion::getPriority));
        
        return sorted.get(sorted.size() - 1);
    }
}
