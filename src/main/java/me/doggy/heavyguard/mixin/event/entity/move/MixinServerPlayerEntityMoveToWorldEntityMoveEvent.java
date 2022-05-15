package me.doggy.heavyguard.mixin.event.entity.move;


import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.event.entity.MoveType;
import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.mixininterfaces.IPosSetChecksSwitchable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerEntityMoveToWorldEntityMoveEvent
{
    @Shadow
    public PortalInfo findDimensionEntryPoint(ServerLevel destination){return null;};
    
    private Location3d _heavyguard_oldMoveToWorldLocation;
    
    @Inject(method = "changeDimension", at = @At(value = "HEAD"), cancellable = true)
    private void heavyguard_onChangeDimension(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> callbackInfo)
    {
        var player = (ServerPlayer)(Object)this;
        var teleportTarget = findDimensionEntryPoint(destination);
        _heavyguard_oldMoveToWorldLocation = new Location3d(player.getLevel(), player.position());
        Location3d newLocation = new Location3d(destination, teleportTarget.pos);
    
        var event = new EntityMoveEvent.CanMove(player, _heavyguard_oldMoveToWorldLocation, newLocation, MoveType.ChangeWorld);
        MinecraftForge.EVENT_BUS.post(event);
        var cancelled = event.isCanceled();
        
        if(cancelled)
        {
            callbackInfo.cancel();
        }
        else
        {
            var entityMixin = ((IPosSetChecksSwitchable)(Object)player);
            entityMixin.heavyguard_disablePosChecks(true);
        }
    }
    
    @Inject(method = "changeDimension",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 5, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void heavyguard_onChangedDimension(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> callbackInfo)
    {
        var player = (ServerPlayer)(Object)this;
        var newLocation = new Location3d(player.getLevel(), player.position());
    
        var event = new EntityMoveEvent.Moved(player, _heavyguard_oldMoveToWorldLocation, newLocation, MoveType.ChangeWorld);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
