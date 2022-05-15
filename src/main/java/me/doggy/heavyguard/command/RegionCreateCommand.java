package me.doggy.heavyguard.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.region.LevelBoundedRegion;
import me.doggy.heavyguard.region.RegionsProvider;
import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.math3d.BoundsInt;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.item.ModItems;
import me.doggy.heavyguard.item.custom.MeasuringTapeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class RegionCreateCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        ModCommands.registerCommand(dispatcher,
                ModCommands.PREFIX_REGION
                        .literal("create")
                        .argument("name", StringArgumentType.word())
                        .executes(c -> execute(c.getSource(), StringArgumentType.getString(c, "name"))));
    }
    
    private static int execute(CommandSourceStack source, String name) throws CommandSyntaxException
    {
        Entity sourceEntity = source.getEntity();
        
        if(sourceEntity instanceof ServerPlayer == false)
            return -1;
        ServerPlayer player = (ServerPlayer)sourceEntity;
        
        for(ItemStack itemStack : player.getHandSlots())
        {
            if(itemStack.getItem() == ModItems.MEASURING_TAPE.get())
            {
                if(MeasuringTapeItem.isBothPointSet(itemStack))
                {
                    ServerLevel world = player.getLevel();
                    IRegionsContainer regions = RegionsProvider.instance().getRegions(world);
                    
                    if(regions.hasRegion(name))
                    {
                        TextBuilder.of("Region with name '" + name + "' already exists in this world.", ChatFormatting.RED).send(player);
                        return 0;
                    }
                    else
                    {
                        BoundsInt regionBounds = MeasuringTapeItem.getBounds(itemStack);
                        var region = new LevelBoundedRegion(name, world, regionBounds);
                        region.Members.setPlayerMembership(player.getUUID(), Membership.Owner);
                        regions.addRegion(region);
                        region.markDirty();
    
                        TextBuilder.of("Region with name '" + name + "' created with bounds " + regionBounds + "!", ChatFormatting.GREEN).send(player);
                        return 1;
                    }
                }
                else
                {
                    TextBuilder.of("You need to chose both corners to create a region.", ChatFormatting.RED).send(player);
                    return 0;
                }
            }
            else
            {
                TextBuilder.of("You need to hold a measuring tape to create a region.", ChatFormatting.RED).send(player);
                return 0;
            }
        }
        return 0;
    }
}
