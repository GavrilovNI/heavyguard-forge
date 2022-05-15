package me.doggy.heavyguard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.command.builder.CommandBuilder;
import net.minecraft.commands.CommandSourceStack;

public class RegionBuilderTestCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated)
    {
        ModCommands.registerCommand(dispatcher,
                CommandBuilder.of("1")
                        .alias("1a1")
                        .alias("1a2")
                        .executes(RegionBuilderTestCommands::execute));
        
        ModCommands.registerCommand(dispatcher,
                CommandBuilder.of("2")
                        .alias("2a1")
                        .literal("2o1", true)
                        .literal("2o2", true)
                        .literal("2l1")
                        .literal("2l1o1", true)
                        .alias("2l1a1")
                        .executes(RegionBuilderTestCommands::execute));
        
        ModCommands.registerCommand(dispatcher,
                CommandBuilder.of("3")
                        .then(CommandBuilder.of("3l1")
                                .literal("3l1o1", true)
                                .literal("3l1l2")
                        )
                        .literal("3o2", true)
                        .executes(RegionBuilderTestCommands::execute));
        
        ModCommands.registerCommand(dispatcher,
                CommandBuilder.of("4")
                        .then(CommandBuilder.of("4l1")
                                .literal("4l1o1", true)
                                .literal("4l1l2")
                        )
                        .literal("4l2")
                        .executes(RegionBuilderTestCommands::execute));
        
        ModCommands.registerCommand(dispatcher,
                CommandBuilder.of("5")
                        .then(CommandBuilder.of("5o1")
                                        .literal("5o1o1", true)
                                        .literal("5o1l2")
                                , true)
                        .literal("5l2")
                        .executes(RegionBuilderTestCommands::execute));
        
        ModCommands.registerCommand(dispatcher,
                CommandBuilder.of("6")
                        .aliases("6a1", "6a2")
                        .literal("6o1", true)
                        .literal("6o2", true)
                        .then( CommandBuilder.of("6l3")
                                .alias("6l3a1")
                                .literal("6l3o1", true)
                        )
                        .literal("6o4", true)
                        .literal("6l5")
                        .then(CommandBuilder.of("6l6")
                                .then(CommandBuilder.of("6l6o1")
                                        .literal("6l6o1o1", true)
                                        , true)
                                .then(CommandBuilder.of("6l6l2")))
                        .alias("6l6a1")
                        .executes(RegionBuilderTestCommands::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        TextBuilder.of("Text Command").send(context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
