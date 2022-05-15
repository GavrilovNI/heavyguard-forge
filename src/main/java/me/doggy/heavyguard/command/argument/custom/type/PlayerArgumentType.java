package me.doggy.heavyguard.command.argument.custom.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.command.argument.ModCommandExceptions;
import me.doggy.heavyguard.util.CommandContextArg;
import me.doggy.heavyguard.util.PlayerUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerArgumentType implements ArgumentType<String>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("DoGGy", "Notch");
    
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException
    {
        String playerName = reader.readUnquotedString();
        if(playerName.isEmpty())
            throw ModCommandExceptions.MISSED_ARGUMENT_EXCEPTION.create(reader.getCursor());
        return playerName;
    }
    
    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return SharedSuggestionProvider.suggest(PlayerUtils.getAllPlayerNames(), builder);
    }
    
    public static PlayerArgumentType player()
    {
        return new PlayerArgumentType();
    }
    
    private static ServerPlayer getOnlinePlayerByName(CommandContext<CommandSourceStack> context, String playerName)
    {
        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        return player;
    }
    
    public static UUID getPlayerUuid(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        String playerName = context.getArgument(name, String.class);
        UUID playerUuid = PlayerUtils.getUUID(playerName);
        if(playerUuid == null)
            throw ModCommandExceptions.PLAYER_NOT_FOUND_EXCEPTION.create(playerName);
        return playerUuid;
    }
    
    public static String getPlayerName(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        String playerName = context.getArgument(name, String.class);
        if(PlayerUtils.contains(playerName) == false)
            throw ModCommandExceptions.PLAYER_NOT_FOUND_EXCEPTION.create(playerName);
        return playerName;
    }
    
    public static ServerPlayer getOnlinePlayer(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        String playerName = context.getArgument(name, String.class);
        var player = getOnlinePlayerByName(context, playerName);
        if(player == null)
            throw ModCommandExceptions.PLAYER_NOT_FOUND_EXCEPTION.create(playerName);
        return player;
    }
    
    public static ServerPlayer getOnlinePlayerOrSource(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        String playerName = CommandContextArg.getOptional(context, name, String.class);
        var player = getOnlinePlayerByName(context, playerName);
        player = context.getSource().getPlayerOrException();
        if(player == null)
            throw ModCommandExceptions.PLAYER_NOT_FOUND_EXCEPTION.create(playerName);
        return player;
    }
    
    public static void register()
    {
        ArgumentTypes.register(new ResourceLocation(HeavyGuard.MOD_ID, "player").toString(),
                PlayerArgumentType.class, new EmptyArgumentSerializer<>(PlayerArgumentType::player));
    }
}
