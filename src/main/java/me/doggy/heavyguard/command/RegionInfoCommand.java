package me.doggy.heavyguard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.command.argument.custom.type.RegionsArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;

public class RegionInfoCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        ModCommands.registerCommand(dispatcher,
                ModCommands.PREFIX_REGION
                        .literal("info")
                        .argument("regions", RegionsArgumentType.regions(), true)
                        .executes(RegionInfoCommand::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        Collection<IRegion> regions = RegionsArgumentType.getRegions(context, "regions");
    
        TextBuilder textBuilder = TextBuilder.of("Found " + regions.size() + " regions:").startNewLine();
        
        for(var region : regions)
            textBuilder.add(region.getTextBuilder()).startNewLine();
        textBuilder.removeLastLine();
        textBuilder.send(source);
        return Command.SINGLE_SUCCESS;
    }
}
