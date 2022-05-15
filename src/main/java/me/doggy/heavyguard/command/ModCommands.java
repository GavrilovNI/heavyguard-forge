package me.doggy.heavyguard.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.command.argument.custom.type.EnumArgumentType;
import me.doggy.heavyguard.command.argument.custom.type.PlayerArgumentType;
import me.doggy.heavyguard.command.argument.custom.type.ParametersArgumentType;
import me.doggy.heavyguard.command.argument.custom.type.RegionsArgumentType;
import me.doggy.heavyguard.command.builder.CommandBuilder;
import me.doggy.heavyguard.command.builder.CommandPrefix;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HeavyGuard.MOD_ID)
public class ModCommands
{
    private static final String[] ALIASES = new String[]{"hg"};
    private static boolean _argumentTypesRegistered = false;
    
    public static final CommandPrefix PREFIX_REGION = CommandPrefix.of(CommandBuilder.of("region").alias("rg"));
    public static final CommandPrefix PREFIX_REGION_SET = CommandPrefix.of(PREFIX_REGION.literal("set"));
    public static final CommandPrefix PREFIX_REGION_REMOVE = CommandPrefix.of(PREFIX_REGION.literal("remove").alias("rm"));
    
    public static LiteralCommandNode registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuilder commandBuilder)
    {
        return CommandBuilder.of(HeavyGuard.MOD_ID).aliases(ALIASES).then(commandBuilder).register(dispatcher);
    }
    
    private static void registerArgumentTypes()
    {
        if(_argumentTypesRegistered)
            return;
        _argumentTypesRegistered = true;
        
        RegionsArgumentType.register();
        PlayerArgumentType.register();
        EnumArgumentType.register();
        ParametersArgumentType.register();
    }
    
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event)
    {
        registerArgumentTypes();
        
        var dispatcher = event.getDispatcher();
        
        //RegionBuilderTestCommands.register(dispatcher);
        RegionCreateCommand.register(dispatcher);
        RegionRemoveCommand.register(dispatcher);
        RegionInfoCommand.register(dispatcher);
        RegionSetFlagCommand.register(dispatcher);
        RegionRemoveFlagCommand.register(dispatcher);
        RegionSetMembershipCommand.register(dispatcher);
    }
}
