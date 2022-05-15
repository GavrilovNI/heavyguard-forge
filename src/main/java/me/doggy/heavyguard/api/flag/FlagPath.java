package me.doggy.heavyguard.api.flag;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FlagPath implements Iterable<String>
{
    public static final String DELIMITER = ".";
    public static final String DELIMITER_REGEX = "\\.";
    
    public static final FlagPath EMPTY = new FlagPath(new ArrayList<>());
    
    private final List<String> _path;
    
    private FlagPath(List<String> path)
    {
        _path = path;
    }
    
    public static FlagPath of(List<String> path)
    {
        return new FlagPath(path.stream().map(x -> x.replace(' ', '_')).toList());
    }
    public static FlagPath of(String... path)
    {
        return FlagPath.of(Arrays.stream(path).toList());
    }
    
    public static FlagPath parse(String path)
    {
        Objects.requireNonNull(path);
        path = path.replace(' ', '_');
        return FlagPath.of(path.split(DELIMITER_REGEX));
    }
    
    public int getLength()
    {
        return _path.size();
    }
    
    public boolean isEmpty()
    {
        return _path.isEmpty();
    }
    
    public String get(int i)
    {
        return _path.get(i);
    }
    
    public FlagPath getSubFlag(int beginIndex, int length)
    {
        return new FlagPath(_path.subList(beginIndex, beginIndex + length));
    }
    
    public FlagPath getSubPath(int beginIndex)
    {
        return new FlagPath(_path.subList(beginIndex, _path.size()));
    }
    
    public FlagPath add(FlagPath flag)
    {
        var path = new ArrayList<>(_path);
        path.addAll(flag._path);
        return new FlagPath(path);
    }
    
    @NotNull
    @Override
    public Iterator<String> iterator()
    {
        return _path.iterator();
    }
    
    @Override
    public String toString()
    {
        return String.join(DELIMITER, _path);
    }
}
