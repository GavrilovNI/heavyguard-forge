package me.doggy.heavyguard.flag.node;

import me.doggy.heavyguard.api.region.IRegion;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlagNodeLiteral extends FlagNode
{
    private final List<String> _aliases;
    
    public FlagNodeLiteral(String name, String... aliases)
    {
        Objects.requireNonNull(name);
        Objects.requireNonNull(aliases);
        
        _aliases = new ArrayList<>();
        _aliases.add(name);
        _aliases.addAll(List.of(aliases));
    }
    
    @Override
    public String getName()
    {
        return _aliases.get(0);
    }
    
    @Override
    public ArrayList<String> getAliases(@Nullable IRegion region)
    {
        return new ArrayList<>(_aliases);
    }
}
