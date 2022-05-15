package me.doggy.heavyguard.flag;

import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.flag.node.FlagNode;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FlagTypePath implements Iterable<FlagNode>
{
    private final List<FlagNode> _path;
    
    private FlagTypePath(List<FlagNode> path)
    {
        _path = path;
    }
    
    public static FlagTypePath of(List<FlagNode> path)
    {
        return new FlagTypePath(new ArrayList<>(path));
    }
    
    public static FlagTypePath of(FlagNode... path)
    {
        return new FlagTypePath(Arrays.stream(path).toList());
    }
    
    public FlagTypePath add(FlagTypePath flagTypePath)
    {
        var path = new ArrayList<>(_path);
        path.addAll(flagTypePath._path);
        return new FlagTypePath(path);
    }
    
    public int getLength()
    {
        return _path.size();
    }
    
    public boolean isEmpty()
    {
        return _path.isEmpty();
    }
    
    public FlagTypePath getSubPath(int beginIndex, int count)
    {
        return new FlagTypePath(_path.subList(beginIndex, beginIndex + count));
    }
    public FlagTypePath getSubPath(int beginIndex)
    {
        return new FlagTypePath(_path.subList(beginIndex, _path.size()));
    }
    
    public FlagNode get(int index)
    {
        return _path.get(index);
    }
    
    @NotNull
    @Override
    public Iterator<FlagNode> iterator()
    {
        return _path.iterator();
    }
    
    public FlagPath toFlagPath()
    {
        return FlagPath.of(_path.stream().map(n -> n.getName()).toList());
    }

    public TextBuilder getInfo(IRegion region)
    {
        ChatFormatting[] colors = new ChatFormatting[] {
                ChatFormatting.AQUA,
                ChatFormatting.GREEN,
                ChatFormatting.LIGHT_PURPLE,
                ChatFormatting.YELLOW
        };
        TextBuilder result = TextBuilder.of();
        for(var node : _path)
        {
            result.add(node.getClass().getSimpleName() + " ");
            var aliases = node.getAliases(region);
            for(int i = 0; i < aliases.size(); i++)
                result.add(aliases.get(i) + " ", colors[i % colors.length]);
            result.startNewLine();
        }
        result.removeLastLine();
        return result;
    }
    
    @Override
    public String toString()
    {
        return toFlagPath().toString();
    }
}
