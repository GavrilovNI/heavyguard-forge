package me.doggy.heavyguard.mixin.event.entity.move;

import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.event.entity.MoveType;
import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.mixininterfaces.IPosSetChecksSwitchable;
import me.doggy.heavyguard.mixininterfaces.ITeleportChecksSwitchable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerEntityTeleportEntityMoveEvent
{
    @Shadow
    public ServerGamePacketListenerImpl connection;
    
    private Location3d _heavyguard_oldTeleportLocation;
    
    @Inject(method = {"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getLevelData()Lnet/minecraft/world/level/storage/LevelData;"),
            cancellable = true)
    public void heavyguard_onTeleport(ServerLevel targetLevel, double x, double y, double z, float yaw, float pitch, CallbackInfo callbackInfo)
    {
        var player = (ServerPlayer)(Object)this;
        _heavyguard_oldTeleportLocation = new Location3d(player.getLevel(), player.position());
        var newLocation = new Location3d(targetLevel, new Vec3(x, y, z));
    
        var event = new EntityMoveEvent.CanMove(player, _heavyguard_oldTeleportLocation, newLocation, MoveType.Teleport);
        MinecraftForge.EVENT_BUS.post(event);
        var cancelled = event.isCanceled();
        
        if(cancelled)
        {
            callbackInfo.cancel();
        }
        else
        {
            var requestTeleportMixin = (ITeleportChecksSwitchable)(Object)connection;
            requestTeleportMixin.heavyguard_disableRequestTeleportChecks(true);
            var posSetMixin = (IPosSetChecksSwitchable)(Object)player;
            posSetMixin.heavyguard_disablePosChecks(true);
        }
    }
    
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
            at = @At(value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public void heavyguard_onTeleported(ServerLevel targetLevel, double x, double y, double z, float yaw, float pitch, CallbackInfo ci)
    {
        var player = (ServerPlayer)(Object)this;
        var newLocation = new Location3d(player.getLevel(), player.position());
    
        var event = new EntityMoveEvent.Moved(player, _heavyguard_oldTeleportLocation, newLocation, MoveType.Teleport);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
