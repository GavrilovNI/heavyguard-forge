package me.doggy.heavyguard.command.argument.custom.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.command.argument.ModCommandExceptions;
import me.doggy.heavyguard.flag.FlagPath;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FlagArgumentType implements ArgumentType<String>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("member.break.block", "player.use.item");
    
    public static boolean isAllowedInFlag(final char c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '-'
                || c == '.' || c == '+'
                || c == ':' || c == '*';
    }
    
    public String readFlag(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedInFlag(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }
    
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException
    {
        String flag = readFlag(reader);
        if(flag.isEmpty())
            throw ModCommandExceptions.MISSED_ARGUMENT_EXCEPTION.create(reader.getCursor());
        return flag;
    }
    
    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return Suggestions.empty();
    }
    
    public static FlagArgumentType flag()
    {
        return new FlagArgumentType();
    }
    
    public static String getFlag(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        return context.getArgument(name, String.class);
    }
    
    public static FlagPath getFlagPath(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        String flag = getFlag(context, name);
        return FlagPath.parse(flag);
    }
    
    public static void register()
    {
        ArgumentTypes.register(new ResourceLocation(HeavyGuard.MOD_ID, "flag").toString(),
                PlayerArgumentType.class, new EmptyArgumentSerializer(FlagArgumentType::flag));
    }
}
