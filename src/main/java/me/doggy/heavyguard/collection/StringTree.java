package me.doggy.heavyguard.collection;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StringTree<T> implements Iterable<Map.Entry<String, StringTree<T>>>
{
    private T _value;
    private final Map<String, StringTree<T>> _children = new HashMap<>();
    
    public StringTree()
    {
        this(null);
    }
    
    public StringTree(@Nullable T value)
    {
        _value = value;
    }
    
    @Nullable
    public T getValue()
    {
        return _value;
    }
    public T setValue(@Nullable T value)
    {
        T oldValue = _value;
        _value = value;
        return oldValue;
    }
    
    public boolean isEmpty()
    {
        return _children.isEmpty();
    }
    
    public void clearChildren()
    {
        _children.clear();
    }
    
    public int getChildrenCount()
    {
        return _children.size();
    }
    
    @Nullable
    public StringTree<T> getChild(String key)
    {
        return _children.get(key);
    }
    
    public StringTree<T> getOrCreateChild(String key)
    {
        var result = getChild(key);
        if(result == null)
        {
            result = new StringTree<>();
            setChild(key, result);
        }
        return result;
    }
    
    @Nullable
    public StringTree<T> setChild(String key, StringTree<T> child)
    {
        return _children.put(key, child);
    }
    
    @Nullable
    public StringTree<T> removeChild(String key)
    {
        return _children.remove(key);
    }
    
    
    @NotNull
    @Override
    public Iterator<Map.Entry<String, StringTree<T>>> iterator()
    {
        return _children.entrySet().iterator();
    }
    
    public static class Serializer<T> implements JsonSerializer<StringTree<T>>, JsonDeserializer<StringTree<T>>
    {
        private final Class<T> _valueType;
    
        public Serializer(Class<T> valueType)
        {
            _valueType = valueType;
        }
    
        @Override
        public JsonElement serialize(StringTree<T> src, @Nullable Type typeOfSrc, @Nullable JsonSerializationContext context)
        {
            JsonObject obj = new JsonObject();
            if(src._value != null)
                obj.add("value", context.serialize(src._value));
    
            if(src._children.isEmpty() == false)
            {
                JsonObject children = new JsonObject();
                for(var child : src)
                    children.add(child.getKey(), context.serialize(child.getValue()));
                obj.add("children", children);
            }
            return obj;
        }
    
        @Override
        public StringTree<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
    
            T value = jsonObject.has("value") ? context.deserialize(jsonObject.get("value"), _valueType) : null;
    
            StringTree<T> tree = new StringTree<>(value);
    
            if(jsonObject.has("children"))
            {
                JsonObject children = jsonObject.get("children").getAsJsonObject();
                for(var childEntry : children.entrySet())
                    tree.setChild(childEntry.getKey(), context.deserialize(childEntry.getValue(), StringTree.class));
            }
            return tree;
        }
    }
}
