package me.doggy.heavyguard.flag;

import com.google.gson.*;
import me.doggy.heavyguard.api.Consts;
import me.doggy.heavyguard.api.event.region.RegionFlagsEvent;
import me.doggy.heavyguard.api.flag.FlagPath;
import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.flag.IRegionFlags;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.collection.StringTree;
import me.doggy.heavyguard.region.RegionPart;
import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("unchecked")
public class RegionFlags extends RegionPart implements IRegionFlags
{
    private final StringTree<Boolean> _tree;
    
    public RegionFlags()
    {
        _tree = new StringTree<>();
    }
    
    public RegionFlags(StringTree<Boolean> tree)
    {
        _tree = tree;
    }
    
    private void removeEmptyNodes(FlagPath path)
    {
        if(path.isEmpty())
            return;
        StringTree<Boolean> lastNotEmpty = null;
        String nodeKeyToRemove = null;
        
        StringTree<Boolean> currentTree = _tree;
        for(var flagNode : path)
        {
            var nextTree = currentTree.getChild(flagNode);
            if(nextTree == null)
                throw new IllegalArgumentException("path is not fully present");
            if(currentTree.getValue() != null || currentTree.getChildrenCount() > 1)
            {
                lastNotEmpty = currentTree;
                nodeKeyToRemove = flagNode;
            }
            currentTree = nextTree;
        }
        if(currentTree.getValue() != null || currentTree.getChildrenCount() > 0)
            return;
        if(lastNotEmpty == null)
            _tree.removeChild(path.get(0));
        else
            lastNotEmpty.removeChild(nodeKeyToRemove);
    }
    
    @Nullable
    private StringTree<Boolean> getNode(FlagPath path, boolean createIfNotExist)
    {
        StringTree<Boolean> currentTree = _tree;
        if(createIfNotExist)
        {
            for(var flagNode : path)
                currentTree = currentTree.getOrCreateChild(flagNode);
        }
        else
        {
            for(var flagNode : path)
            {
                var nextTree = currentTree.getChild(flagNode);
                if(nextTree == null)
                    return null;
                currentTree = nextTree;
            }
        }
        return currentTree;
    }
    
    private boolean setNodeValue(StringTree<Boolean> node, Boolean value, FlagPath path)
    {
        var oldValue = node.setValue(value);
        if(oldValue == null && value != null || oldValue != null && oldValue.equals(value) == false)
        {
            postEventByRegion(region -> new RegionFlagsEvent.FlagUpdated(region, path, value));
            markDirty();
            return true;
        }
        return false;
    }
    
    public void removeValue(FlagPath path)
    {
        Objects.requireNonNull(path);
        StringTree<Boolean> node = getNode(path, false);
        if(node == null)
            return;
        boolean updated = setNodeValue(node, null, path);
        if(updated)
            removeEmptyNodes(path);
    }
    
    public void setValue(FlagPath path, boolean value)
    {
        Objects.requireNonNull(path);
        StringTree<Boolean> node = getNode(path, true);
        setNodeValue(node, value, path);
    }
    
    public void setValue(FlagPath path, @Nullable Boolean value)
    {
        if(value == null)
            removeValue(path);
        else
            setValue(path, value);
    }
    
    @Nullable
    public Boolean getValue(FlagPath path)
    {
        Objects.requireNonNull(path);
    
        StringTree<Boolean> currentTree = _tree;
        for(var flagNode : path)
        {
            var nextChild = currentTree.getChild(flagNode);
            if(nextChild == null)
                return null;
            else
                currentTree = nextChild;
        }
        return currentTree.getValue();
    }
    
    @Nullable
    private Boolean getValue(FlagTypePath path, IRegion region, StringTree<Boolean> currentTree)
    {
        var aliases = path.get(0).getAliases(region);
        aliases.add(Consts.FLAG_ALIAS_ANY);
        
        for(var alias : aliases)
        {
            var nextChild = currentTree.getChild(alias);
            if(nextChild == null)
                continue;
            
            Boolean result;
            if(path.getLength() == 1)
                result = nextChild.getValue();
            else
                result = getValue(path.getSubPath(1), region, nextChild);
    
            if(result != null)
                return result;
        }
        return currentTree.getValue();
    }
    
    @Nullable
    public Boolean getValue(FlagTypePath path)
    {
        Objects.requireNonNull(path);
        
        if(path.isEmpty())
            return _tree.getValue();
        
        return getValue(path, getRegion(), _tree);
    }
    
    public void remove(FlagPath path)
    {
        Objects.requireNonNull(path);
    
        if(path.isEmpty())
        {
            if(_tree.isEmpty())
                return;
            _tree.clearChildren();
        }
        else
        {
            StringTree<Boolean> prev = null;
            StringTree<Boolean> current = _tree;
    
            for(var flagNode : path)
            {
                var nextChild = current.getChild(flagNode);
                if(nextChild == null)
                    return;
                prev = current;
                current = nextChild;
            }
            prev.setChild(path.get(path.getLength() - 1), null);
        }
    
        postEventByRegion(region -> new RegionFlagsEvent.FlagRemoved(region, path));
        markDirty();
    }
    
    @Override
    public String toString()
    {
        return getTextBuilder().toString();
    }
    
    @Override
    public TextBuilder getTextBuilder()
    {
        Map<Boolean, ChatFormatting> colors = Map.of(
                true, ChatFormatting.GREEN,
                false, ChatFormatting.RED
        );
        
        TextBuilder builder = TextBuilder.of();
        var allPaths = getAllFlagPaths();
        var keys = allPaths.keySet().stream().sorted(Comparator.comparing(FlagPath::toString)).toList();
        for(var path : keys)
        {
            Boolean value = allPaths.get(path);
            builder.add(path + " = ");
            builder.add(value.toString(), colors.get(value));
            builder.startNewLine();
        }
        builder.removeLastLine();
        return builder;
    }

    public Map<FlagPath, Boolean> getAllFlagPaths()
    {
        return getAllFlagPaths(FlagPath.EMPTY, _tree);
    }
    private Map<FlagPath, Boolean> getAllFlagPaths(FlagPath prefix, StringTree<Boolean> currentNode)
    {
        HashMap<FlagPath, Boolean> result = new HashMap<>();
        var value = currentNode.getValue();
        if(value != null)
        {
            result.put(prefix, value);
        }
        for(var child : currentNode)
        {
            var childResult = getAllFlagPaths(prefix.add(FlagPath.of(child.getKey())), child.getValue());
            result.putAll(childResult);
        }
        return result;
    }
    
    public static class Serializer implements JsonSerializer<RegionFlags>, JsonDeserializer<RegionFlags>
    {
        @Override
        public JsonElement serialize(RegionFlags src, @Nullable Type typeOfSrc, @Nullable JsonSerializationContext context)
        {
            return new GsonBuilder().registerTypeAdapter(StringTree.class, new StringTree.Serializer(Boolean.class)).create().toJsonTree(src._tree);
        }
        
        @Override
        public RegionFlags deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            var tree = new GsonBuilder().registerTypeAdapter(StringTree.class, new StringTree.Serializer(Boolean.class)).create().fromJson(json, StringTree.class);
            return new RegionFlags(tree);
        }
    }
}
