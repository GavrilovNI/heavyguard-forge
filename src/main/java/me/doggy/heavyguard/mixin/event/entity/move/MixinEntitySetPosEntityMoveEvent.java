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


@Mixin(Entity.class)
public class MixinEntitySetPosEntityMoveEvent implements IPosSetChecksSwitchable
{
    @Shadow
    private int getId(){return 0;}
    @Shadow
    private Level getLevel(){return null;}
    @Shadow
    private void setDeltaMovement(double x, double y, double z){}
    @Shadow
    private Vec3 position;
    
    private Vec3 _heavyguard_oldPosition;
    
    private boolean _heavyguard_checksDisabled = false;
    private boolean _heavyguard_checksDisabledOnce = false;
    public void heavyguard_disablePosChecks(boolean once)
    {
        _heavyguard_checksDisabled = true;
        _heavyguard_checksDisabledOnce = once;
    }
    public void heavyguard_enablePosChecks()
    {
        _heavyguard_checksDisabled = false;
    }
    
    @Inject(method = "setPosRaw",
            at = @At(value = "NEW", target = "net/minecraft/world/phys/Vec3"),
            cancellable = true)
    private void heavyguard_onSettingRawPos(double x, double y, double z, CallbackInfo callbackInfo)
    {
        if(_heavyguard_checksDisabled)
            return;
        
        var level = getLevel();
        boolean isEntityLoaded = level != null && level.getEntity(getId()) != null;
        
        if(isEntityLoaded == false)
            return;
        
        _heavyguard_oldPosition = position;
        var oldLocation = new Location3d(level, position);
        var newLocation = new Location3d(level, new Vec3(x, y, z));
        
        var entity = (Entity)(Object) this;
        
        var event = new EntityMoveEvent.CanMove(entity, oldLocation, newLocation, MoveType.Simple);
        MinecraftForge.EVENT_BUS.post(event);
        var cancelled = event.isCanceled();
        
        if(cancelled)
        {
            setDeltaMovement(0,0,0);
            callbackInfo.cancel();
        }
    }
    
    @Inject(method = "setPosRaw",
            at = @At(value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/entity/EntityInLevelCallback;onMove()V"))
    private void heavyguard_onSetRawPos(double x, double y, double z, CallbackInfo ci)
    {
        if(_heavyguard_checksDisabled)
        {
            if(_heavyguard_checksDisabledOnce)
                _heavyguard_checksDisabled = false;
            return;
        }
        var world = getLevel();
        boolean isEntityLoaded = world != null && world.getEntity(getId()) != null;
        if(isEntityLoaded == false)
            return;
        
        var oldLocation = new Location3d(world, _heavyguard_oldPosition);
        var newLocation = new Location3d(world, new Vec3(x, y, z));
        
        var entity = (Entity)(Object) this;
        var event = new EntityMoveEvent.Moved(entity, oldLocation, newLocation, MoveType.Simple);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
