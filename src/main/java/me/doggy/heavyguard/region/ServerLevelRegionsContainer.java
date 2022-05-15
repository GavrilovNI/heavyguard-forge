package me.doggy.heavyguard.region;

import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.event.region.BoundedRegionEvent;
import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IBoundedRegion;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.flag.FlagTypePath;
import me.doggy.heavyguard.math3d.MultiMap3D;
import me.doggy.heavyguard.util.LevelUtils;
import me.doggy.heavyguard.util.VectorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerLevelRegionsContainer implements IRegionsContainer
{
    private final IEventBus _eventBus = BusBuilder.builder().build();
    
    private final HashSet<String> _removedRegionNames = new HashSet<>();
    
    private final Map<String, IRegion> _regionsByName = new HashMap<>();
    private final MultiMap3D<IBoundedRegion> _regionsByPosition = new MultiMap3D<>(128);
    private final Set<IRegion> _globalRegions = new HashSet<>();
    
    private final ServerLevel _level;
    
    public ServerLevelRegionsContainer(ServerLevel level)
    {
        _level = level;
        subscribeToEvents();
    }
    
    @SubscribeEvent
    public void onRegionBoundsUpdated(BoundedRegionEvent.BoundsUpdated event)
    {
        var region = event.getRegion();
        if(hasRegion(region))
        {
            var oldBounds = event.getOldBounds();
            _regionsByPosition.remove(oldBounds, region);
            _regionsByPosition.put(region.getBounds(), region);
        }
    }
    
    @Override
    public IEventBus getEventBus()
    {
        return _eventBus;
    }
    
    @Override
    public ServerLevel getLevel()
    {
        return _level;
    }
    
    private boolean canInteract(FlagTypePath path, Collection<IRegion> regions)
    {
        for(var region : regions)
        {
            if(region.canInteract(path) == false)
                return false;
        }
        return true;
    }
    
    private boolean canInteract(FlagTypePath path, Vec3 position)
    {
        var regions = getRegions(position);
        return canInteract(path, regions);
    }
    
    private<T extends Player> void sendDebugToAll(FlagTypePath path, Vec3 pos)
    {
        sendDebug(path, getRegions(pos), _level.getPlayers(p -> true), "");
    }
    private<T extends Player> void sendDebugToAll(FlagTypePath path, Vec3 pos, String message)
    {
        sendDebug(path, getRegions(pos), _level.getPlayers(p -> true), message);
    }
    private<T extends Player> void sendDebugToAll(FlagTypePath path, Collection<IRegion> regions, String message)
    {
        sendDebug(path, regions, _level.getPlayers(p -> true), message);
    }
    private<T extends Player> void sendDebugToAll(FlagTypePath path, Collection<IRegion> regions)
    {
        sendDebugToAll(path, regions, "");
    }
    private<T extends Player> void sendDebug(FlagTypePath path, Collection<IRegion> regions, Collection<T> players)
    {
        sendDebug(path, regions, players, "");
    }
    private<T extends Player> void sendDebug(FlagTypePath path, Collection<IRegion> regions, Collection<T> players, String message)
    {
        for(var player : players)
        {
            for(var region : regions)
            {
                TextBuilder.of("Interaction for " + region.getName()).startNewLine(2)
                        .add(path.getInfo(region)).startNewLine(0).add(message).send(player, ChatType.CHAT);
            }
        }
    }
    private<T extends Player> void sendDebug(FlagTypePath path, Vec3 pos, Collection<T> players)
    {
        sendDebug(path, getRegions(pos), players);
    }
    private<T extends Player> void sendDebug(FlagTypePath path, Collection<IRegion> regions, T... players)
    {
        sendDebug(path, regions, Arrays.stream(players).toList());
    }
    private<T extends Player> void sendDebug(FlagTypePath path, Vec3 pos, T... players)
    {
        sendDebug(path, getRegions(pos), players);
    }
    
    private<T extends Player> void sendNoAccessMessage(FlagTypePath path, T... players)
    {
        sendNoAccessMessage(path, Arrays.stream(players).toList());
    }
    private<T extends Player> void sendNoAccessMessage(FlagTypePath path, Collection<T> players)
    {
        for(var player : players)
            TextBuilder.of("You don't have region permission '" + path + "' to do that.", ChatFormatting.RED).send(player, ChatType.GAME_INFO);
    }
    
    public void subscribeToEvents()
    {
    
//        AttackBlockCallback.EVENT.register(
//                (Player player, Level world, Hand hand, BlockPos pos, Direction direction) -> {
//                    if(getLevel() != world)
//                        return ActionResult.PASS;
//
//                    FlagTypePath path = FlagTypePath.of(
//                            new FlagNodePlayer(player),
//                            new FlagNodeLiteral("attack"),
//                            new FlagNodeLevelBlock(new Location3i(world, pos))
//                    );
//
//                    Vec3 interactPosition = Vec3.of(pos);
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebug(path, interactPosition, player);
//                    if(canInteract == false)
//                        sendNoAccessMessage(path, player);
//                    return canInteract ? ActionResult.PASS : ActionResult.FAIL;
//                });
//        PlayerBlockBreakEvents.BEFORE.register(
//                (Level world, Player player, BlockPos pos, BlockState state, /* Nullable */ BlockEntity blockEntity) -> {
//                    if(getLevel() != world)
//                        return true;
//
//                    FlagTypePath path = FlagTypePath.of(
//                            new FlagNodePlayer(player),
//                            new FlagNodeLiteral("break"),
//                            new FlagNodeLevelBlock(new Location3i(world, pos))
//                    );
//
//                    Vec3 interactPosition = Vec3.of(pos);
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebug(path, interactPosition, player);
//                    if(canInteract == false)
//                        sendNoAccessMessage(path, player);
//                    return canInteract;
//                });
//        UseBlockCallback.EVENT.register(
//                (Player player, Level world, Hand hand, BlockHitResult hitResult) -> {
//                    if(getLevel() != world)
//                        return ActionResult.PASS;
//
//                    var pos = hitResult.getBlockPos();
//
//                    FlagTypePath path = FlagTypePath.of(
//                            new FlagNodePlayer(player),
//                            new FlagNodeLiteral("use"),
//                            new FlagNodeLevelBlock(new Location3i(world, pos))
//                    );
//
//                    Vec3 interactPosition = Vec3.of(pos);
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebug(path, interactPosition, player);
//                    if(canInteract)
//                    {
//                        ItemStack itemStack = player.getStackInHand(hand);
//                        if(itemStack.isEmpty() == false)
//                        {
//                            var actionResult = UseItemCallback.EVENT.invoker().interact(player, world, hand);
//                            canInteract = actionResult.getResult() == ActionResult.PASS;
//                        }
//                    }
//                    else
//                    {
//                        sendNoAccessMessage(path, player);
//                    }
//                    return canInteract ? ActionResult.PASS : ActionResult.FAIL;
//                });
//
//        EntityLevelInteractionEvents.BREAK_BLOCK.register(
//                (Entity entity, Level world, BlockPos pos, boolean drop) -> {
//                    if(getLevel() != world)
//                        return true;
//
//                    FlagTypePath path = FlagTypePath.of(
//                            FlagNodeEntity.create(entity),
//                            new FlagNodeLiteral("break"),
//                            new FlagNodeLiteral("block"),
//                            new FlagNodeLevelBlock(new Location3i(world, pos))
//                    );
//
//                    Vec3 interactPosition = Vec3.of(pos);
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebugToAll(path, interactPosition, "result: "+canInteract);
//                    return canInteract;
//                });
//        EntityLevelInteractionEvents.CHANGE_BLOCK.register(
//                (Entity entity, Level world, BlockPos pos, BlockState newState) -> {
//                    if(getLevel() != world)
//                        return true;
//
//                    boolean fromAir = world.isAir(pos);
//
//                    FlagTypePath path;
//
//                    if(fromAir)
//                    {
//                        path = FlagTypePath.of(
//                                FlagNodeEntity.create(entity),
//                                new FlagNodeLiteral("place"),
//                                new FlagNodeLiteral("block"),
//                                new FlagNodeBlock(newState.getBlock())
//                        );
//                    }
//                    else
//                    {
//                        path = FlagTypePath.of(
//                                FlagNodeEntity.create(entity),
//                                new FlagNodeLiteral("change"),
//                                new FlagNodeLiteral("block"),
//                                new FlagNodeLevelBlock(new Location3i(world, pos)),
//                                new FlagNodeBlock(newState.getBlock())
//                        );
//                    }
//
//
//                    Vec3 interactPosition = Vec3.of(pos);
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebugToAll(path, interactPosition, "result: "+canInteract);
//                    return canInteract;
//                });
//
//        AttackEntityCallback.EVENT.register(
//                (Player player, Level world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) -> {
//                    if(getLevel() != world)
//                        return ActionResult.PASS;
//
//                    Vec3 interactPosition = hitResult == null ? entity.getPos() : hitResult.getPos();
//
//                    FlagTypePath path = FlagTypePath.of(
//                            new FlagNodePlayer(player),
//                            new FlagNodeLiteral("attack"),
//                            FlagNodeEntity.create(entity)
//                    );
//
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebug(path, interactPosition, player);
//                    if(canInteract == false)
//                        sendNoAccessMessage(path, player);
//                    return canInteract ? ActionResult.PASS : ActionResult.FAIL;
//                });
//        UseEntityCallback.EVENT.register(
//                (Player player, Level world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) -> {
//                    if(getLevel() != world)
//                        return ActionResult.PASS;
//
//                    Vec3 interactPosition = hitResult == null ? entity.getPos() : hitResult.getPos();
//
//                    FlagTypePath path = FlagTypePath.of(
//                            new FlagNodePlayer(player),
//                            new FlagNodeLiteral("use"),
//                            FlagNodeEntity.create(entity)
//                    );
//
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebug(path, interactPosition, player);
//                    if(canInteract == false)
//                        sendNoAccessMessage(path, player);
//                    return canInteract ? ActionResult.PASS : ActionResult.FAIL;
//                });
//        UseItemCallback.EVENT.register(
//                (Player player, Level world, Hand hand) -> {
//                    if(getLevel() != world)
//                        return TypedActionResult.pass(ItemStack.EMPTY);
//
//                    Item item = player.getStackInHand(hand).getItem();
//
//                    Vec3 interactPosition = player.getPos();
//
//                    FlagTypePath path = FlagTypePath.of(
//                            new FlagNodePlayer(player),
//                            new FlagNodeLiteral("use"),
//                            new FlagNodeItem(item)
//                    );
//
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebug(path, interactPosition, player);
//                    if(canInteract == false)
//                        sendNoAccessMessage(path, player);
//                    return canInteract ? TypedActionResult.pass(ItemStack.EMPTY) : TypedActionResult.fail(ItemStack.EMPTY);
//                });
//        EntityMoveEvent.ENTITY_CAN_MOVE.register(
//                (Entity entity, Location3d oldLocation, Location3d newLocation, MoveType moveType ) -> {
//                    if(getLevel() != oldLocation.getLevel())
//                        return true;
//                    ServerLevel oldServerLevel = (ServerLevel)oldLocation.getLevel();
//                    ServerLevel newServerLevel = (ServerLevel)newLocation.getLevel();
//                    var oldPositionRegions = RegionsProvider.instance().getRegions(oldServerLevel).getRegions(oldLocation.getPosition());
//                    var newPositionRegions = RegionsProvider.instance().getRegions(newServerLevel).getRegions(newLocation.getPosition());
//
//                    Function<Entity, HashSet<Player>> getPlayersFromSelfAndPassengers = new Function<>()
//                    {
//                        @Override
//                        public HashSet<Player> apply(Entity entity)
//                        {
//                            HashSet result = new HashSet();
//                            if(entity.isPlayer())
//                                result.add(entity);
//                            for(var passenger : entity.getPassengerList())
//                                result.addAll(this.apply(passenger));
//                            return result;
//                        }
//                    };
//
//
//                    Func3<Entity, FlagTypePath, HashSet<IRegion>, Boolean> canEntityAndPassengerInteract = new Func3<Entity, FlagTypePath, HashSet<IRegion>, Boolean>()
//                    {
//                        @Override
//                        public Boolean apply(Entity entity, FlagTypePath subPath, HashSet<IRegion> regions)
//                        {
//                            FlagTypePath flagPath = FlagTypePath.of(
//                                    FlagNodeEntity.create(entity)
//                            ).add(subPath);
//
//                            sendDebug(flagPath, regions, getPlayersFromSelfAndPassengers.apply(entity));
//
//                            boolean result = canInteract(flagPath, regions);
//                            if(result)
//                            {
//                                for(var passenger : entity.getPassengerList())
//                                    if(this.apply(passenger, subPath, regions) == false)
//                                        return false;
//                            }
//                            return result;
//                        }
//                    };
//
//
//                    var regions = new HashSet<>(newPositionRegions);
//                    regions.removeAll(oldPositionRegions);
//
//                    String moveTypeStr = moveType.name().toLowerCase();
//
//                    FlagTypePath subPath = FlagTypePath.of(new FlagNodeLiteral("enter"),
//                            new FlagNodeLiteral(moveTypeStr));
//
//                    boolean canDo = canEntityAndPassengerInteract.apply(entity, subPath, regions);
//
//                    if(canDo)
//                    {
//                        regions = new HashSet<>(oldPositionRegions);
//                        regions.removeAll(newPositionRegions);
//
//                        subPath = FlagTypePath.of(new FlagNodeLiteral("leave"),
//                                new FlagNodeLiteral(moveTypeStr));
//                        canDo = canEntityAndPassengerInteract.apply(entity, subPath, regions);
//                    }
//
//                    if(canDo == false)
//                    {
//                        FlagTypePath pathToSend = FlagTypePath.of(FlagNodeEntity.create(entity)).add(subPath);
//                        sendNoAccessMessage(pathToSend, getPlayersFromSelfAndPassengers.apply(entity));
//                    }
//
//                    return canDo;
//                });
//        FluidFlowEvent.FLOW.register(
//                (FlowableFluid fluid, LevelAccess world, BlockPos oldPos, BlockPos newPos, Direction direction) -> {
//                    if(getLevel() != world)
//                        return true;
//                    ServerLevel serverLevel = (ServerLevel)world;
//                    var oldPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(oldPos));
//                    var newPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(newPos));
//
//                    var regions = new HashSet<>(newPositionRegions);
//                    regions.removeAll(oldPositionRegions);
//
//                    FlagTypePath prefix = FlagTypePath.of(new FlagNodeFluid(fluid));
//
//                    FlagTypePath flagPath = prefix.add(FlagTypePath.of(new FlagNodeLiteral("enter")));
//                    boolean canDo = canInteract(flagPath, regions);
//                    sendDebugToAll(flagPath, regions, "result = " + canDo);
//
//                    if(canDo)
//                    {
//                        regions = new HashSet<>(oldPositionRegions);
//                        regions.removeAll(newPositionRegions);
//
//                        flagPath = prefix.add(FlagTypePath.of(new FlagNodeLiteral("leave")));
//                        canDo = canInteract(flagPath, regions);
//                        sendDebugToAll(flagPath, regions, "result = " + canDo);
//                    }
//                    return canDo;
//                });
//        PistonEvents.MOVE.register(
//                (Level world, BlockPos pistonBlockPos, BlockPos blockToMovePos, Direction dir) -> {
//                    if(getLevel() != world)
//                        return true;
//                    ServerLevel serverLevel = (ServerLevel)world;
//
//                    var newBlockPosition = blockToMovePos.offset(dir);
//
//                    var pistonPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(pistonBlockPos));
//                    var oldBlockPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(blockToMovePos));
//                    var newBlockPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(newBlockPosition));
//
//
//                    FlagTypePath prefix = FlagTypePath.of(
//                            new FlagNodeLevelBlock(new Location3i(world, pistonBlockPos), "piston")
//                    );
//
//                    var regions = new HashSet<>(oldBlockPositionRegions);
//                    regions.removeAll(pistonPositionRegions);
//
//                    FlagTypePath flagPath = prefix.add(FlagTypePath.of(
//                            new FlagNodeLiteral("move"),
//                            new FlagNodeLiteral("from_outside"),
//                            new FlagNodeLevelBlock(new Location3i(world, blockToMovePos))
//                    ));
//                    boolean canDo = canInteract(flagPath, regions);
//                    sendDebugToAll(flagPath, regions, "result: " + canDo);
//
//                    if(canDo)
//                    {
//                        regions = new HashSet<>(newBlockPositionRegions);
//                        regions.removeAll(oldBlockPositionRegions);
//                        regions.removeAll(pistonPositionRegions);
//
//                        flagPath = prefix.add(FlagTypePath.of(
//                                new FlagNodeLiteral("insert"),
//                                new FlagNodeLiteral("block"),
//                                new FlagNodeLiteral("from_outside"),
//                                new FlagNodeLevelBlock(new Location3i(world, blockToMovePos))
//                        ));
//                        canDo = canInteract(flagPath, regions);
//                        sendDebugToAll(flagPath, regions, "result: " + canDo);
//                    }
//
//                    if(canDo)
//                    {
//                        regions = new HashSet<>(oldBlockPositionRegions);
//                        regions.removeAll(newBlockPositionRegions);
//                        regions.removeAll(pistonPositionRegions);
//
//                        flagPath = prefix.add(FlagTypePath.of(
//                                new FlagNodeLiteral("extract"),
//                                new FlagNodeLiteral("block"),
//                                new FlagNodeLiteral("from_outside"),
//                                new FlagNodeLevelBlock(new Location3i(world, blockToMovePos))
//                        ));
//                        canDo = canInteract(flagPath, regions);
//                        sendDebugToAll(flagPath, regions, "result: " + canDo);
//                    }
//                    return canDo;
//                });
//        PistonEvents.EXTEND.register(
//                (Level world, BlockPos pistonBlockPos, Direction dir) -> {
//                    if(getLevel() != world)
//                        return true;
//                    ServerLevel serverLevel = (ServerLevel)world;
//                    BlockPos bodyPos = pistonBlockPos;
//                    BlockPos headPos = pistonBlockPos.offset(dir);
//
//                    var bodyPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(bodyPos));
//                    var headPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(headPos));
//
//                    FlagTypePath prefix = FlagTypePath.of(
//                            new FlagNodeLevelBlock(new Location3i(world, pistonBlockPos), "piston")
//                    );
//
//                    var regions = new HashSet<>(headPositionRegions);
//                    regions.removeAll(bodyPositionRegions);
//                    FlagTypePath flagPath = prefix.add(FlagTypePath.of(
//                            new FlagNodeLiteral("extend"),
//                            new FlagNodeLiteral("from_outside")
//                    ));
//                    boolean canDo = canInteract(flagPath, regions);
//                    sendDebugToAll(flagPath, regions, "result: " + canDo);
//                    return canDo;
//                });
//        PistonEvents.SHRINK.register(
//                (Level world, BlockPos pistonBlockPos, Direction pistonDir) -> {
//                    if(getLevel() != world)
//                        return true;
//                    ServerLevel serverLevel = (ServerLevel)world;
//                    BlockPos bodyPos = pistonBlockPos;
//                    BlockPos headPos = pistonBlockPos.offset(pistonDir);
//
//                    var bodyPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(bodyPos));
//                    var headPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(headPos));
//
//                    FlagTypePath prefix = FlagTypePath.of(
//                            new FlagNodeLevelBlock(new Location3i(world, pistonBlockPos), "piston")
//                    );
//
//                    var regions = new HashSet<>(headPositionRegions);
//                    regions.removeAll(bodyPositionRegions);
//                    FlagTypePath flagPath = prefix.add(FlagTypePath.of(
//                            new FlagNodeLiteral("shrink"),
//                            new FlagNodeLiteral("from_outside")
//                    ));
//                    boolean canDo = canInteract(flagPath, regions);
//                    sendDebugToAll(flagPath, regions, "result: " + canDo);
//                    return canDo;
//                });
//        PistonEvents.BREAK.register(
//                (Level world, BlockPos pistonBlockPos, BlockPos blockToBreakPos) -> {
//                    if(getLevel() != world)
//                        return true;
//                    ServerLevel serverLevel = (ServerLevel)world;
//
//                    var pistonPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(pistonBlockPos));
//                    var blockPositionRegions = RegionsProvider.instance().getRegions(serverLevel).getRegions(Vec3.of(blockToBreakPos));
//
//                    var regions = new HashSet<>(blockPositionRegions);
//                    regions.removeAll(pistonPositionRegions);
//
//                    FlagTypePath flagPath = FlagTypePath.of(
//                            new FlagNodeLevelBlock(new Location3i(world, pistonBlockPos), "piston"),
//                            new FlagNodeLiteral("break"),
//                            new FlagNodeLiteral("from_outside"),
//                            new FlagNodeLevelBlock(new Location3i(world, blockToBreakPos)
//                            ));
//                    boolean canDo = canInteract(flagPath, regions);
//                    sendDebugToAll(flagPath, regions, "result: " + canDo);
//
//                    return canDo;
//                });
//        CollisionEvents.ENTITY_COLLIDE_BLOCK.register(
//                (Level world, BlockPos pos, Entity entity) -> {
//                    if(getLevel() != world)
//                        return true;
//                    boolean isAir = world.getBlockState(pos).getBlock() == Blocks.AIR;
//                    if(isAir)
//                        return true;
//
//                    Vec3 interactPosition = Vec3.of(pos);
//
//                    FlagTypePath path = FlagTypePath.of(
//                            FlagNodeEntity.create(entity),
//                            new FlagNodeLiteral("collide"),
//                            new FlagNodeLevelBlock(new Location3i(world, pos))
//                    );
//
//                    boolean canInteract = canInteract(path, interactPosition);
//
//                    if(entity instanceof Player player)
//                    {
//                        sendDebug(path, interactPosition, player);
//                        if(canInteract == false)
//                            sendNoAccessMessage(path, player);
//                    }
//                    return canInteract;
//                });
//
//
//        FlagTypePath redstonePath = FlagTypePath.of(new FlagNodeLiteral("redstone"));
//        FlagTypePath redstonePowerSpreadPath = redstonePath.add(FlagTypePath.of(new FlagNodeLiteral("spread_power")));
//        FlagTypePath redstoneWireConnectPath = redstonePath.add(FlagTypePath.of(new FlagNodeLiteral("connect_wire_through_border")));
//
//        RedstoneEvent.RedstonePowerSpread redstoneCanSpread =
//                (BlockView spreadLevel, BlockPos from, BlockPos to) -> {
//                    ServerLevel world = getLevel();
//                    if(world != spreadLevel)
//                        return true;
//
//                    var oldPositionRegions = RegionsProvider.instance().getRegions(world).getRegions(Vec3.of(from));
//                    var newPositionRegions = RegionsProvider.instance().getRegions(world).getRegions(Vec3.of(to));
//
//                    var regions = new HashSet<>(newPositionRegions);
//                    regions.removeAll(oldPositionRegions);
//
//                    FlagTypePath flagPath = redstonePowerSpreadPath.add(FlagTypePath.of(new FlagNodeLiteral("from_outside")));
//                    boolean canDo = canInteract(flagPath, regions);
//                    String posInfo = " from: " + from + " to: " + to;
//                    sendDebugToAll(flagPath, regions, "result: " + canDo + posInfo);
//
//                    if(canDo)
//                    {
//                        regions = new HashSet<>(oldPositionRegions);
//                        regions.removeAll(newPositionRegions);
//
//                        flagPath = redstonePowerSpreadPath.add(FlagTypePath.of(new FlagNodeLiteral("from_inside")));
//                        canDo = canInteract(flagPath, regions);
//                        sendDebugToAll(flagPath, regions, "result: " + canDo + posInfo);
//                    }
//
//                    return canDo;
//                };
//
//        RedstoneEvent.WEAK_POWER_SPREAD.register(redstoneCanSpread);
//        RedstoneEvent.STRONG_POWER_SPREAD.register(redstoneCanSpread);
//        RedstoneEvent.REDSTONE_WIRE_CONNECT.register(
//                (BlockView spreadLevel, BlockPos from, BlockPos to) -> {
//                    ServerLevel world = getLevel();
//                    if(world != spreadLevel)
//                        return true;
//
//                    var oldPositionRegions = RegionsProvider.instance().getRegions(world).getRegions(Vec3.of(from));
//                    var newPositionRegions = RegionsProvider.instance().getRegions(world).getRegions(Vec3.of(to));
//
//                    var regionsEntering = new HashSet<>(newPositionRegions);
//                    regionsEntering.removeAll(oldPositionRegions);
//
//                    var regions = new HashSet<>(oldPositionRegions);
//                    regions.removeAll(newPositionRegions);
//                    regions.addAll(regionsEntering);
//
//                    FlagTypePath flagPath = redstoneWireConnectPath;
//                    boolean canDo = canInteract(flagPath, regions);
//                    String posInfo = " from: " + from + " to: " + to;
//                    sendDebugToAll(flagPath, regions, "result: " + canDo + posInfo);
//
//                    return canDo;
//                });
//
//        ExplosionEvent.EXPLODE.register(
//                (Level world, BlockPos pos) -> {
//                    if(getLevel() != world)
//                        return true;
//
//                    FlagTypePath path = FlagTypePath.of(
//                            new FlagNodeLiteral("explosion"),
//                            new FlagNodeLiteral("break"),
//                            new FlagNodeLiteral("block"),
//                            new FlagNodeLevelBlock(new Location3i(world, pos))
//                    );
//
//                    Vec3 interactPosition = Vec3.of(pos);
//                    boolean canInteract = canInteract(path, interactPosition);
//                    sendDebug(path, interactPosition);
//                    return canInteract;
//                });
    }
    
    public void load()
    {
        HeavyGuard.LOGGER.info("Loading regions in world " + LevelUtils.getName(getLevel()));
        var stats = ServerLevelRegionsLoader.loadRegions(this);
        HeavyGuard.LOGGER.info("Regions loaded in world " + LevelUtils.getName(getLevel()) + " " + stats);
    }
    
    public void save()
    {
        HeavyGuard.LOGGER.info("Saving regions in world " + LevelUtils.getName(getLevel()));
        
        var removedRegions = ServerLevelRegionsLoader.removeRegionsFromFiles(getLevel(), _removedRegionNames);
        _removedRegionNames.removeAll(removedRegions);
        if(_removedRegionNames.isEmpty() == false)
            HeavyGuard.LOGGER.error("Couldn't remove some region folders on save: " + _removedRegionNames.stream().toList());
        
        var stats = ServerLevelRegionsLoader.saveRegions(this);
        
        HeavyGuard.LOGGER.info("Regions saved in world " + LevelUtils.getName(getLevel()) + " " + stats);
    }
    
    @Override
    public void addRegion(IRegion region)
    {
        String name = region.getName();
        if(_regionsByName.containsKey(name))
            throw new IllegalArgumentException("Region with same name already contains in this world.");
        if(getLevel().equals(region.getLevel()) == false)
            throw new IllegalArgumentException("Region's world is not the world you try to add in.");
        
        _removedRegionNames.remove(name);
        _regionsByName.put(name, region);
        if(region instanceof LevelBoundedRegion boundedRegion)
            _regionsByPosition.put(boundedRegion.getBounds(), boundedRegion);
        else
            _globalRegions.add(region);
        
        region.getEventBus().register(this);
        RegionEvent.postEventBy(this, new RegionEvent.Added(region));
    }
    
    @Override
    public void removeRegion(IRegion region)
    {
        if(_regionsByName.containsValue(region) == false)
            throw new IllegalArgumentException("This region does not contains in this world.");
    
        region.getEventBus().unregister(this);
        RegionEvent.postEventBy(this, new RegionEvent.Removing(region));
        
        String regionName = region.getName();
        _removedRegionNames.add(regionName);
        _regionsByName.remove(regionName);
        if(region instanceof LevelBoundedRegion boundedRegion)
            _regionsByPosition.remove(boundedRegion.getBounds(), boundedRegion);
        else
            _globalRegions.remove(region);
    }
    
    public void removeRegion(String name)
    {
        var region = _regionsByName.get(name);
        if(region == null)
            throw new IllegalArgumentException("Region with this name does not contains in this world.");
        removeRegion(region);
    }
    
    @Override
    public void clear()
    {
        _regionsByName.clear();
        _regionsByPosition.clear();
        _globalRegions.clear();
        _removedRegionNames.clear();
    }
    
    @Override
    public boolean hasRegion(IRegion region)
    {
        Objects.requireNonNull(region);
        var regionWithSameName = getRegion(region.getName());
        return region == regionWithSameName;
    }
    
    @Override
    public boolean hasRegion(String name)
    {
        return _regionsByName.containsKey(name);
    }
    
    @Override
    public Set<IRegion> getGlobalRegions()
    {
        return new HashSet<>(_globalRegions);
    }
    
    @Override
    public Set<IBoundedRegion> getBoundedRegions(Vec3 position)
    {
        Vec3i positionInt = VectorUtils.doubleToInt(position, d -> (int)Math.floor(d));
        return _regionsByPosition.get(positionInt).stream().filter(
                r -> r.contains(position)).collect(Collectors.toSet());
    }
    
    @Override
    public Set<IBoundedRegion> getBoundedRegions(BoundsInt bounds)
    {
        var min = bounds.getMin();
        var max = bounds.getMax();
        
        var delta = _regionsByPosition.delta;
        
        Set<IBoundedRegion> result = new HashSet<>();
        
        for(int z = min.getZ(); z < max.getZ(); z += delta)
        {
            for(int y = min.getY(); y < max.getY(); y += delta)
            {
                for(int x = min.getX(); x < max.getX(); x += delta)
                {
                    Vec3i pos = new Vec3i(x, y, z);
                    var regionsOnPosition = _regionsByPosition.get(pos);
                    regionsOnPosition.forEach(region -> {
                        var closestPosition = region.getBounds().getClosestPosition(Vec3.atLowerCornerOf(pos));
                        if(bounds.contains(closestPosition))
                            result.add(region);
                    });
                }
            }
        }
        return result;
    }
    
    @Override
    public Stream<IRegion> getRegions(Predicate<? super IRegion> predicate)
    {
        return _regionsByName.values().stream().filter(predicate);
    }
    
    @Nullable
    @Override
    public IRegion getRegion(String name)
    {
        return _regionsByName.get(name);
    }
    
    @NotNull
    @Override
    public Iterator<IRegion> iterator()
    {
        return _regionsByName.values().iterator();
    }
    
}
