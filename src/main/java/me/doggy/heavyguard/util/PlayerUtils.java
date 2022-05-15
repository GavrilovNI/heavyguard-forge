package me.doggy.heavyguard.util;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerUtils
{
    public static boolean contains(UUID uuid)
    {
        return ServerGetter.getServer().getProfileCache().get(uuid).isEmpty() == false;
    }
    public static boolean contains(String name)
    {
        return ServerGetter.getServer().getProfileCache().get(name).isEmpty() == false;
    }
    
    public static final Set<String> getAllPlayerNames()
    {
        return Arrays.stream(ServerGetter.getServer().getPlayerNames()).collect(Collectors.toSet());
    }
    
    @Nullable
    public static String getName(UUID uuid)
    {
        var profileOpt = ServerGetter.getServer().getProfileCache().get(uuid);
        if(profileOpt.isEmpty())
            return null;
        return profileOpt.get().getName();
    }
    
    @Nullable
    public static UUID getUUID(String name)
    {
        var profileOpt = ServerGetter.getServer().getProfileCache().get(name);
        if(profileOpt.isEmpty())
            return null;
        return profileOpt.get().getId();
    }
    
    public static String getNameOrDefault(UUID uuid, String defaultName)
    {
        String name = getName(uuid);
        return name == null ? defaultName : name;
    }
    
    public static String getNameOrUuid(UUID uuid)
    {
        return getNameOrDefault(uuid, uuid.toString());
    }
}
