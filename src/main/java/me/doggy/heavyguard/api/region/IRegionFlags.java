package me.doggy.heavyguard.api.region;

import me.doggy.heavyguard.api.utils.ITextable;
import me.doggy.heavyguard.flag.FlagPath;
import me.doggy.heavyguard.flag.FlagTypePath;

import javax.annotation.Nullable;

public interface IRegionFlags extends ITextable
{
    void setValue(FlagPath path, @Nullable Boolean value);
    
    @Nullable
    Boolean getValue(FlagPath path);
    
    @Nullable
    Boolean getValue(FlagTypePath path);
    
    void remove(FlagPath path);
}
