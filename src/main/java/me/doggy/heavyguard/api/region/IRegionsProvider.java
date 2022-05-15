package me.doggy.heavyguard.api.region;


import net.minecraft.server.level.ServerLevel;

public interface IRegionsProvider
{
    IRegionsContainer getRegions(ServerLevel level);
}
