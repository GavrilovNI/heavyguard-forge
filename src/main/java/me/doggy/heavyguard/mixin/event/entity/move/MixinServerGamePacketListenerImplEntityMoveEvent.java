package me.doggy.heavyguard.mixin.event.entity.move;

import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.event.entity.MoveType;
import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.mixininterfaces.IPosSetChecksSwitchable;
import me.doggy.heavyguard.mixininterfaces.ITeleportChecksSwitchable;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImplEntityMoveEvent implements ITeleportChecksSwitchable
{
    @Shadow
    private ServerPlayer player;
    @Shadow
    private void teleport(double x, double y, double z, float yaw, float pitch){}
    @Shadow
    private Vec3 awaitingPositionFromClient;
    
    private Vec3 _heavyguard_playerPositionBeforeMove;
    
    private boolean _heavyguard_checksDisabled = false;
    private boolean _heavyguard_checksDisabledOnce = false;
    @Override
    public void heavyguard_enableRequestTeleportChecks()
    {
        _heavyguard_checksDisabled = false;
    }
    @Override
    public void heavyguard_disableRequestTeleportChecks(boolean once)
    {
        _heavyguard_checksDisabled = true;
        _heavyguard_checksDisabledOnce = once;
    }
    
    @Inject(method = {"teleport(DDDFFLjava/util/Set;Z)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;absMoveTo(DDDFF)V"))
    private void heavyguard_onRequestTeleport(double x, double y, double z, float yaw, float pitch,
                                              Set<ClientboundPlayerPositionPacket.RelativeArgument> flags,
                                              boolean shouldDismount, CallbackInfo callbackInfo)
    {
        if(_heavyguard_checksDisabled)
            return;
        else if(_heavyguard_checksDisabledOnce)
            _heavyguard_checksDisabled = false;
        
        var level = player.getLevel();
        var oldLocation = new Location3d(level, player.position());
        var newLocation = new Location3d(level, awaitingPositionFromClient);
        
        var event = new EntityMoveEvent.CanMove(player, oldLocation, newLocation, MoveType.Teleport);
        MinecraftForge.EVENT_BUS.post(event);
        var cancelled = event.isCanceled();
        
        if(cancelled)
            awaitingPositionFromClient = oldLocation.getPosition();
        
        var playerMixin = ((IPosSetChecksSwitchable)(Object)player);
        playerMixin.heavyguard_disablePosChecks(true);
    }
    
    @ModifyVariable(method = "teleport(DDDFFLjava/util/Set;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;absMoveTo(DDDFF)V"),
            ordinal = 0)
    public double heavyguard_modifyX(double oldX)
    {
        return awaitingPositionFromClient.x;
    }
    @ModifyVariable(method = "teleport(DDDFFLjava/util/Set;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;absMoveTo(DDDFF)V"),
            ordinal = 1)
    public double heavyguard_modifyY(double oldY)
    {
        return awaitingPositionFromClient.y;
    }
    @ModifyVariable(method = "teleport(DDDFFLjava/util/Set;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;absMoveTo(DDDFF)V"),
            ordinal = 2)
    public double heavyguard_modifyZ(double oldZ)
    {
        return awaitingPositionFromClient.z;
    }
    
    
    @Inject(method = "handleMovePlayer",
            at = @At(value = "HEAD"))
    private void heavyguard_onOnPlayerMoveHead(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo)
    {
        var playerSetPosMixin = (IPosSetChecksSwitchable)(Object)this.player;
        playerSetPosMixin.heavyguard_disablePosChecks(false);
        heavyguard_disableRequestTeleportChecks(false);
    }
    
    @Inject(method = {"handleMovePlayer"},
            at = @At(value = "NEW", target = "net/minecraft/world/phys/Vec3"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void heavyguard_onHandlingPlayerMove(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo, ServerLevel serverLevel,
                                                 double d0, double d1, double d2, float f, float f1,
                                                 double d3, double d4, double d5, double d6, double d7, double d8, double d9)
    {
        var level = player.getLevel();
        Vec3 oldPos = player.position();
        _heavyguard_playerPositionBeforeMove = oldPos;
        Location3d oldLocation = new Location3d(level, oldPos);
        Location3d newLocation = new Location3d(level, oldPos.add(new Vec3(d7, d8, d9)));
    
        var event = new EntityMoveEvent.CanMove(player, oldLocation, newLocation, MoveType.Simple);
        MinecraftForge.EVENT_BUS.post(event);
        var cancelled = event.isCanceled();
        
        if(cancelled)
        {
            teleport(oldPos.x, oldPos.y, oldPos.z, f, f1);
            var playerSetPosMixin = (IPosSetChecksSwitchable)(Object)this.player;
            playerSetPosMixin.heavyguard_enablePosChecks();
            heavyguard_enableRequestTeleportChecks();
            callbackInfo.cancel();
        }
    }
    
    @Inject(method = {"handleMovePlayer"},
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 7, target = "Lnet/minecraft/server/level/ServerPlayer;getZ()D"))
    private void heavyguard_onPlayerMoved(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo)
    {
        var level = player.getLevel();
        Location3d oldLocation = new Location3d(level, _heavyguard_playerPositionBeforeMove);
        Location3d newLocation = new Location3d(level, player.position());
        
        var event = new EntityMoveEvent.Moved(player, oldLocation, newLocation, MoveType.Simple);
        MinecraftForge.EVENT_BUS.post(event);
    }
    
    @Inject(method = {"handleMovePlayer"},
            at = @At(value = "RETURN"))
    private void heavyguard_onHandleMovePlayerReturn(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo)
    {
        var playerSetPosMixin = (IPosSetChecksSwitchable)(Object)this.player;
        playerSetPosMixin.heavyguard_enablePosChecks();
        heavyguard_enableRequestTeleportChecks();
    }
}
