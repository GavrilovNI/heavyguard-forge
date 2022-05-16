package me.doggy.heavyguard.api.interaction;

import me.doggy.heavyguard.api.region.IRegionsProvider;

public interface Interaction
{
    public InteractionResult test(IRegionsProvider regionsProvider);
}
