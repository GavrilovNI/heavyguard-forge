package me.doggy.heavyguard.mixininterfaces;

public interface ITeleportChecksSwitchable
{
    void heavyguard_disableRequestTeleportChecks(boolean once);
    void heavyguard_enableRequestTeleportChecks();
}
