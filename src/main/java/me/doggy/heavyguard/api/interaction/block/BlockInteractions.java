package me.doggy.heavyguard.api.interaction.block;

import me.doggy.heavyguard.api.math3d.Location3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;

public abstract class BlockInteractions
{
    public static class EntityAttackBlock extends BlockInteraction.SimpleEntityInteractWithBlock
    {
        public EntityAttackBlock(Location3i<ServerLevel> blockLocation, Entity interactor)
        {
            super(blockLocation, interactor, "attack");
        }
        
        @Nullable
        public static EntityAttackBlock create(PlayerInteractEvent.LeftClickBlock event)
        {
            var levelAccessor = event.getWorld();
            if(levelAccessor instanceof ServerLevel level)
                return new EntityAttackBlock(new Location3i(level, event.getPos()), event.getPlayer());
            else
                return null;
        }
    }
    
    public static class EntityBreakBlock extends BlockInteraction.SimpleEntityInteractWithBlock
    {
        public EntityBreakBlock(Location3i<ServerLevel> blockLocation, Entity interactor)
        {
            super(blockLocation, interactor, "break");
        }
    
        @Nullable
        public static EntityBreakBlock create(BlockEvent.BreakEvent event)
        {
            var levelAccessor = event.getWorld();
            if(levelAccessor instanceof ServerLevel level)
                return new EntityBreakBlock(new Location3i(level, event.getPos()), event.getPlayer());
            else
                return null;
        }
    }
    
    public static class EntityPlaceBlock extends BlockInteraction.SimpleEntityInteractWithBlock
    {
        public EntityPlaceBlock(Location3i<ServerLevel> blockLocation, Entity interactor)
        {
            super(blockLocation, interactor, "place");
        }
    
        @Nullable
        public static EntityPlaceBlock create(BlockEvent.EntityPlaceEvent event)
        {
            var levelAccessor = event.getWorld();
            if(levelAccessor instanceof ServerLevel level)
                return new EntityPlaceBlock(new Location3i(level, event.getPos()), event.getEntity());
            else
                return null;
        }
    }
    
    public static class EntityUseBlock extends BlockInteraction.SimpleEntityInteractWithBlock
    {
        public EntityUseBlock(Location3i<ServerLevel> blockLocation, Entity interactor)
        {
            super(blockLocation, interactor, "use");
        }
        
        @Nullable
        public static EntityUseBlock create(PlayerInteractEvent.RightClickBlock event)
        {
            var levelAccessor = event.getWorld();
            if(levelAccessor instanceof ServerLevel level)
            {
                var pos = event.getPos();
                var block = level.getBlockState(pos);
                if(block.isAir())
                    return null;
                else
                    return new EntityUseBlock(new Location3i<>(level, pos), event.getPlayer());
            }
            else
            {
                return null;
            }
        }
    }
    
}
