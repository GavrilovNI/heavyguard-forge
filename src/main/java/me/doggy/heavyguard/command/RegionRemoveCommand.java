package me.doggy.heavyguard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.region.RegionsProvider;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.command.argument.custom.type.RegionsArgumentType;
import me.doggy.heavyguard.util.LevelUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

public class RegionRemoveCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        ModCommands.registerCommand(dispatcher,
                ModCommands.PREFIX_REGION_REMOVE
                        .literal("region")
                        .argument("region", RegionsArgumentType.regions())
                        .executes(RegionRemoveCommand::execute)
        );
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var region = RegionsArgumentType.getOneRegion(context, "region");
        var level = region.getLevel();
        IRegionsContainer regions = HeavyGuard.getRegionsProvider().getRegions(level);
        regions.removeRegion(region);
    
        TextBuilder.of("Region '" + region.getName() + "' removed from level " + LevelUtils.getName(level) + ".", ChatFormatting.GREEN).send(context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
