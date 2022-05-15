package me.doggy.heavyguard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.doggy.heavyguard.api.Membership;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.command.argument.custom.type.EnumArgumentType;
import me.doggy.heavyguard.command.argument.custom.type.PlayerArgumentType;
import me.doggy.heavyguard.command.argument.custom.type.RegionsArgumentType;
import me.doggy.heavyguard.util.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

import java.util.UUID;

public class RegionSetMembershipCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        ModCommands.registerCommand(dispatcher,
                ModCommands.PREFIX_REGION_SET
                        .literal("membership")
                        .argument("region", RegionsArgumentType.regions())
                        .argument("membership", EnumArgumentType.enum$(Membership.class))
                        .argument("player", PlayerArgumentType.player(), true)
                        .executes(RegionSetMembershipCommand::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        IRegion region = RegionsArgumentType.getOneRegion(context, "region");
        Membership membership = EnumArgumentType.getEnum(context, "membership");
        UUID playerUuid = PlayerArgumentType.getPlayerUuid(context, "player");
        
        region.getMembers().setPlayerMembership(playerUuid, membership);
    
        String playerName = PlayerUtils.getName(playerUuid);
        TextBuilder.of("Player '" + playerName + "' is now " + membership.name() + " in region " + region.getName(), ChatFormatting.GREEN).send(context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}

