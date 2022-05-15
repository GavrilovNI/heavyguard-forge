package me.doggy.heavyguard.api.interaction.block;

import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.api.flag.node.FlagNodeLiteral;
import me.doggy.heavyguard.api.flag.node.FlagNodeWorldBlock;
import me.doggy.heavyguard.api.flag.node.entity.FlagNodeEntity;
import me.doggy.heavyguard.api.interaction.IInteractedByEntity;
import me.doggy.heavyguard.api.interaction.Interaction;
import me.doggy.heavyguard.api.math3d.Location3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public abstract class BlockInteraction extends Interaction
{
    private final Location3i<ServerLevel> _blockLocation;
    
    public BlockInteraction(Location3i<ServerLevel> blockLocation)
    {
        super(blockLocation.to3d());
        _blockLocation = blockLocation;
    }
    
    public Location3i<ServerLevel> getBlockLocation()
    {
        return _blockLocation;
    }
    
    public abstract static class EntityInteractWithBlock extends BlockInteraction implements IInteractedByEntity
    {
        private final Entity _interactor;
    
        public EntityInteractWithBlock(Location3i<ServerLevel> blockLocation, Entity interactor)
        {
            super(blockLocation);
            _interactor = interactor;
        }
        
        @Override
        public Entity getInteractor()
        {
            return _interactor;
        }
    }
    
    public static class SimpleEntityInteractWithBlock extends EntityInteractWithBlock
    {
        private final String _key;
        
        public SimpleEntityInteractWithBlock(Location3i<ServerLevel> blockLocation, Entity interactor, String interactionKey)
        {
            super(blockLocation, interactor);
            _key = interactionKey;
        }
    
        @Override
        public FlagTypePath getFlagPath()
        {
            return FlagTypePath.of(
                    FlagNodeEntity.create(getInteractor()),
                    new FlagNodeLiteral(_key),
                    new FlagNodeWorldBlock(getBlockLocation())
            );
        }
    }
}
