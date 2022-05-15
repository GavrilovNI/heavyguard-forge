package me.doggy.heavyguard.item.custom;

import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.item.ModItems;
import me.doggy.heavyguard.util.CompoundTagHelper;
import me.doggy.heavyguard.util.LevelUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MeasuringTapeItem extends Item
{
    private static final String FIRST_POINT_NBT_KEY = "measuringPointA";
    private static final String SECOND_POINT_NBT_KEY = "measuringPointB";
    private static final String WORLD_NBT_KEY = "measuringWorld";
    
    static
    {
        Consumer<PlayerInteractEvent> clickHandler =
                (PlayerInteractEvent event) -> {
                    if(event.getWorld() instanceof ServerLevel level)
                    {
                        Player player = event.getPlayer();
                        ItemStack itemStack = player.getItemInHand(event.getHand());
                        Item item = itemStack.getItem();
                        if(item != ModItems.MEASURING_TAPE.get())
                            return;
    
                        boolean firstPoint;
                        if(event instanceof PlayerInteractEvent.LeftClickBlock)
                            firstPoint = true;
                        else if(event instanceof PlayerInteractEvent.RightClickBlock)
                            firstPoint = false;
                        else
                            throw new IllegalArgumentException("event must be block click event");
    
                        BlockPos pos = event.getPos();
    
                        MeasuringTapeItem.registerPoint(itemStack, level, pos, firstPoint);
                        event.setCanceled(true);
    
                        TextBuilder.of((firstPoint ? "First" : "Second") + " point set to (" + pos.toShortString() + ")", ChatFormatting.GREEN).send(player, ChatType.CHAT);
    
                    }
                };
        
        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> clickHandler.accept(event));
        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> clickHandler.accept(event));
    }
    
    public MeasuringTapeItem(Item.Properties properties)
    {
        super(properties.stacksTo(1));
    }
    
    public static void registerPoint(ItemStack itemStack, ServerLevel level, BlockPos pos, boolean firstPoint)
    {
        CompoundTag nbt = itemStack.getOrCreateTag();
        
        if(nbt.contains(WORLD_NBT_KEY) && level != CompoundTagHelper.getServerLevel(nbt, WORLD_NBT_KEY))
            nbt.remove(firstPoint ? SECOND_POINT_NBT_KEY : FIRST_POINT_NBT_KEY);
        
        nbt.putString(WORLD_NBT_KEY, LevelUtils.getIdentifier(level).toString());
        CompoundTagHelper.putLevel(nbt, WORLD_NBT_KEY, level);
        CompoundTagHelper.putVec3i(nbt, firstPoint ? FIRST_POINT_NBT_KEY : SECOND_POINT_NBT_KEY, pos);
        //itemStack.setTag(nbt);
    }
    
    public static boolean isBothPointSet(ItemStack itemStack)
    {
        CompoundTag nbt = itemStack.getOrCreateTag();
        return nbt.contains(FIRST_POINT_NBT_KEY) && nbt.contains(SECOND_POINT_NBT_KEY);
    }
    
    @Nullable
    public static ServerLevel getServerLevel(ItemStack itemStack)
    {
        CompoundTag nbt = itemStack.getOrCreateTag();
        return CompoundTagHelper.getServerLevel(nbt, WORLD_NBT_KEY);
    }
    
    @Nullable
    public static ClientLevel getClientLevel(ItemStack itemStack)
    {
        CompoundTag nbt = itemStack.getOrCreateTag();
        return CompoundTagHelper.getClientLevel(nbt, WORLD_NBT_KEY);
    }
    
    // if no blocks set returns null;
    // if both points set return bounds
    // if not returns set block as bounds;
    @Nullable
    public static BoundsInt getBounds(ItemStack itemStack)
    {
        CompoundTag nbt = itemStack.getOrCreateTag();
        boolean hasFirst = nbt.contains(FIRST_POINT_NBT_KEY);
        boolean hasSecond = nbt.contains(SECOND_POINT_NBT_KEY);
        
        if(hasFirst)
        {
            Vec3i firstPos = CompoundTagHelper.getVec3i(nbt, FIRST_POINT_NBT_KEY);
            if(hasSecond)
            {
                Vec3i secondPos = CompoundTagHelper.getVec3i(nbt, SECOND_POINT_NBT_KEY);
                return new BoundsInt(firstPos, secondPos, true);
            }
            else
            {
                return new BoundsInt(firstPos, firstPos, true);
            }
        }
        else
        {
            if(hasSecond)
            {
                Vec3i secondPos = CompoundTagHelper.getVec3i(nbt, SECOND_POINT_NBT_KEY);
                return new BoundsInt(secondPos, secondPos, true);
            }
            else
            {
                return null;
            }
        }
    }
}
