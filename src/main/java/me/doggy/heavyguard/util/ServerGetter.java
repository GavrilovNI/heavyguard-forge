package me.doggy.heavyguard.util;

import me.doggy.heavyguard.HeavyGuard;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = HeavyGuard.MOD_ID)
public class ServerGetter
{
    private static MinecraftServer _server = null;
    
    @Nullable
    public static MinecraftServer getServer()
    {
        return _server;
    }
    
    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event)
    {
        _server = event.getServer();
    }
    
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent server)
    {
        _server = null;
    }
}
