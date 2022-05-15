package me.doggy.heavyguard.mixin.event.entity.move;

import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.event.entity.MoveType;
import me.doggy.heavyguard.api.math3d.Location3d;
import me.doggy.heavyguard.mixininterfaces.IPosSetChecksSwitchable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class MixinEntityStartRidingEntityMoveEvent
{
    @Shadow
    public abstract double getX();
    
    @Shadow public abstract double getZ();
    
    @Shadow public abstract Level getLevel();
    
    private boolean _heavyguard_justStartedRiding = false;
    private Vec3 _heavyguard_positionBeforeStartedRiding;
    
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "RETURN"))
    private void heavyguard_onStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        _heavyguard_justStartedRiding = callbackInfo.getReturnValue();
    }
    
    @Inject(method = "removeVehicle", at = @At(value = "HEAD"))
    private void heavyguard_onDismountVehicle(CallbackInfo callbackInfo)
    {
        _heavyguard_justStartedRiding = false;
    }
    
    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity$MoveFunction;accept(Lnet/minecraft/world/entity/Entity;DDD)V"),
            cancellable = true)
    private void heavyguard_onUpdatePassengerPosition(Entity passenger, Entity.MoveFunction positionUpdater, CallbackInfo callbackInfo, double d)
    {
        var passengerMixin = (MixinEntityStartRidingEntityMoveEvent)(Object)passenger;
        
        if(passengerMixin._heavyguard_justStartedRiding == false)
            return;
        passengerMixin._heavyguard_justStartedRiding = false;
        
        var level = getLevel();
        _heavyguard_positionBeforeStartedRiding = passenger.position();
        var oldLocation = new Location3d(level, _heavyguard_positionBeforeStartedRiding);
        var newLocation = new Location3d(level, new Vec3(getX(), d, getZ()));
        
        var event = new EntityMoveEvent.CanMove(passenger, oldLocation, newLocation, MoveType.StartRiding);
        MinecraftForge.EVENT_BUS.post(event);
        var cancelled = event.isCanceled();
        
        if(cancelled)
        {
            passenger.removeVehicle();
            callbackInfo.cancel();
        }
        else
        {
            var entityMixin = ((IPosSetChecksSwitchable)(Object)passenger);
            entityMixin.heavyguard_disablePosChecks(true);
        }
    }
    
    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity$MoveFunction;accept(Lnet/minecraft/world/entity/Entity;DDD)V"),
            cancellable = true)
    private void heavyguard_onPassengerPositionUpdated(Entity passenger, Entity.MoveFunction positionUpdater, CallbackInfo callbackInfo, double d)
    {
        var level = getLevel();
        var oldLocation = new Location3d(level, _heavyguard_positionBeforeStartedRiding);
        var newLocation = new Location3d(level, passenger.position());
    
        var event = new EntityMoveEvent.Moved(passenger, oldLocation, newLocation, MoveType.StartRiding);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
