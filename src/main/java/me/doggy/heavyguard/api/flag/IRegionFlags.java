package me.doggy.heavyguard.api.flag;

import me.doggy.heavyguard.api.utils.ITextable;

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
