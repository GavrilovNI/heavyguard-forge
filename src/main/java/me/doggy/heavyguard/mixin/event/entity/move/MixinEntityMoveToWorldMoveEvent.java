package me.doggy.heavyguard.mixin.event.entity.move;

import me.doggy.heavyguard.api.event.entity.EntityMoveEvent;
import me.doggy.heavyguard.api.event.entity.MoveType;
import me.doggy.heavyguard.api.math3d.Location3d;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public class MixinEntityMoveToWorldMoveEvent
{
    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/ITeleporter;placeEntity(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;FLjava/util/function/Function;)Lnet/minecraft/world/entity/Entity;"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void onChangeDimension(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> callbackInfo, PortalInfo portalInfo)
    {
        Entity entity = (Entity)(Object)this;
        var oldLevel = (ServerLevel)entity.getLevel();
        var oldLocation = new Location3d(oldLevel, entity.position());
        var newLocation = new Location3d(destination, portalInfo.pos);
        
        var event = new EntityMoveEvent.CanMove(entity, oldLocation, newLocation, MoveType.ChangeWorld);
        MinecraftForge.EVENT_BUS.post(event);
        var cancelled = event.isCanceled();
        
        if(cancelled)
        {
            callbackInfo.setReturnValue(null);
            callbackInfo.cancel();
        }
    }
    
    // TODO : I have to figure out how to use lambdas in mixins. By the way, it's gonna work without this, but event will be called second time with strange values(actually no, entity is not loaded in world, so event won't be called second time)
    /*@Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;restoreFrom(Lnet/minecraft/world/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onEntityCopied(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> callbackInfo
                                Entity entity)
    {
        //var entityMixin = ((IPosSetChecksSwitchable)(Object)entity);
        //entityMixin.mixin_disablePosChecks(true);
    }*/
    
    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;removeAfterChangingDimensions()V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onChangedDimension(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> callbackInfo, PortalInfo portalInfo, Entity transportedEntity)
    {
        Entity oldEntity = (Entity)(Object)this;
        var oldLocation = new Location3d(oldEntity.getLevel(), oldEntity.position());
        var newLocation = new Location3d(transportedEntity.getLevel(), transportedEntity.position());
        
        var event = new EntityMoveEvent.Moved(transportedEntity, oldLocation, newLocation, MoveType.ChangeWorld);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
