package me.doggy.heavyguard.command.argument.custom.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.doggy.heavyguard.HeavyGuard;
import me.doggy.heavyguard.region.RegionsProvider;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.region.IRegionsContainer;
import me.doggy.heavyguard.command.argument.ModCommandExceptions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RegionsArgumentType implements ArgumentType<Map<String, Object>>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("myRegion",
            "myRegion -w " + Level.OVERWORLD.location().toString(), "myRegion -p 0 15 74");
    
    private static final ParametersArgumentType parameters = ParametersArgumentType.parameters(
            Map.of("", RegionNameArgumentType.name(), "-w", DimensionArgument.dimension(), "-p",
                    Vec3Argument.vec3()), Map.of("-w", Set.of(""), "-p", Set.of("")));
    
    public class RegionInfo
    {
        @Nullable
        public String name;
    }
    
    @Override
    public Map<String, Object> parse(StringReader reader) throws CommandSyntaxException
    {
        return parameters.parse(reader);
    }
    
    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return parameters.listSuggestions(context, builder);
    }
    
    public static RegionsArgumentType regions()
    {
        return new RegionsArgumentType();
    }
    
    public static Collection<IRegion> getRegions(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        var parameters = ParametersArgumentType.getParameters(context, name);
        String regionName = ParametersArgumentType.getParameter(context, parameters, "",
                RegionNameArgumentType::getName);
        ServerLevel level = ParametersArgumentType.getParameter(context, parameters, "-w",
                DimensionArgument::getDimension);
        Vec3 position = ParametersArgumentType.getParameter(context, parameters, "-p", Vec3Argument::getVec3);
    
        CommandSourceStack source = context.getSource();
        IRegionsContainer regionsContainer = RegionsProvider.instance().getRegions(level == null ? source.getLevel() : level);
        if(regionName == null)
        {
            if(position == null)
            {
                if(level == null)
                    position = source.getPosition();
                else
                    return regionsContainer.getRegions();
            }
            return regionsContainer.getRegions(position);
        }
        else
        {
            IRegion region = regionsContainer.getRegion(regionName);
            if(region == null)
                return new ArrayList<>();
            else
                return List.of(region);
        }
    }
    
    public static IRegion getOneRegion(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
    {
        var regions = getRegions(context, name);
        if(regions.size() == 0)
            throw ModCommandExceptions.REGION_NOT_FOUND_EXCEPTION.create(null);
        if(regions.size() > 1)
            throw ModCommandExceptions.REGION_FOUND_TO_MANY.create(null);
        return regions.iterator().next();
    }
    
    public static void register()
    {
        ArgumentTypes.register(new ResourceLocation(HeavyGuard.MOD_ID, "regions").toString(),
                RegionsArgumentType.class,
                new EmptyArgumentSerializer(RegionsArgumentType::regions));
    }
}
