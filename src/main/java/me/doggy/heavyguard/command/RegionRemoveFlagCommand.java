package me.doggy.heavyguard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.command.argument.custom.type.FlagArgumentType;
import me.doggy.heavyguard.command.argument.custom.type.RegionsArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

public class RegionRemoveFlagCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        ModCommands.registerCommand(dispatcher,
                ModCommands.PREFIX_REGION_REMOVE
                        .literal("flag")
                        .argument("region", RegionsArgumentType.regions())
                        .argument("flag", FlagArgumentType.flag())
                        .executes(RegionRemoveFlagCommand::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        IRegion region = RegionsArgumentType.getOneRegion(context, "region");
        var flagPath = FlagArgumentType.getFlagPath(context, "flag");
        
        region.getFlags().setValue(flagPath, null);
    
        TextBuilder.of("Flag '" + flagPath + "' removed in region " + region.getName(), ChatFormatting.GREEN).send(context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}

