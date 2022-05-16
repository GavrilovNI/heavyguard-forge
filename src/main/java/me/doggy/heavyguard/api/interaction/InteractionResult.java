package me.doggy.heavyguard.api.interaction;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Objects;

public class InteractionResult
{
    private final boolean _isCancelled;
    private final BaseComponent _cancelInfo;
    
    public static InteractionResult cancel(BaseComponent cancelInfo)
    {
        return new InteractionResult(true, cancelInfo);
    }
    public static InteractionResult pass()
    {
        return new InteractionResult(false, new TextComponent(""));
    }
    public static InteractionResult of(boolean isCancelled, BaseComponent cancelInfo)
    {
        Objects.requireNonNull(cancelInfo);
        return new InteractionResult(isCancelled, isCancelled ? cancelInfo : new TextComponent(""));
    }
    
    private InteractionResult(boolean isCancelled, BaseComponent cancelInfo)
    {
        _isCancelled = isCancelled;
        _cancelInfo = cancelInfo;
    }
    
    public boolean isCancelled()
    {
        return _isCancelled;
    }
    
    public BaseComponent getCancelInfo()
    {
        return _cancelInfo;
    }
}
