package me.doggy.heavyguard.region;

import me.doggy.heavyguard.api.event.region.RegionEvent;
import me.doggy.heavyguard.api.region.IRegion;
import me.doggy.heavyguard.api.utils.TextBuilder;
import me.doggy.heavyguard.api.flag.FlagTypePath;
import me.doggy.heavyguard.flag.RegionFlags;
import me.doggy.heavyguard.util.LevelUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

public class LevelRegion extends Pollutable implements IRegion, ICleanable
{
    private final IEventBus _eventBus = BusBuilder.builder().build();
    
    protected final String _name;
    protected final ServerLevel _world;
    
    public final RegionMembers Members;
    public final RegionFlags Flags;
    
    public int _priority;
    
    public LevelRegion(String name, ServerLevel world)
    {
        this(name, world, new RegionFlags(), new RegionMembers(), 0);
    }
    public LevelRegion(String name, ServerLevel world, RegionFlags flags, RegionMembers members, int priority)
    {
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("name is null or empty");
        if(world == null)
            throw new NullPointerException("world is null");
        _name = name.toLowerCase();
        _world = world;
        Members = members;
        Flags = flags;
    
        Members.setRegion(this);
        Flags.setRegion(this);
        _priority = priority;
    }
    
    @Override
    public int getPriority()
    {
        return _priority;
    }
    
    @Override
    public void setPriority(int priority)
    {
        if(_priority == priority)
            return;
        var oldPriority = _priority;
        _priority = priority;
        markDirty();
        RegionEvent.postEventBy(this, new RegionEvent.PriorityChanged(this, oldPriority, _priority));
    }
    
    @Override
    public IEventBus getEventBus()
    {
        return _eventBus;
    }
    
    public ServerLevel getLevel()
    {
        return _world;
    }
    
    public String getName()
    {
        return _name;
    }
    
    @Override
    public RegionMembers getMembers()
    {
        return Members;
    }
    
    @Override
    public RegionFlags getFlags()
    {
        return Flags;
    }
    
    public boolean contains(Vec3 position)
    {
        return true;
    }
    
    public boolean canInteract(FlagTypePath path)
    {
        var value = Flags.getValue(path);
        return value == null ? false : value;
    }
    
    @Override
    public void markClean()
    {
        super.markClean();
        Members.markClean();
        Flags.markClean();
    }
    
    @Override
    public boolean isDirty()
    {
        return super.isDirty() || Members.isDirty() || Flags.isDirty();
    }
    
    @Override
    public String toString()
    {
        return getTextBuilder().toString();
    }
    
    @Override
    public TextBuilder getTextBuilder()
    {
        return TextBuilder.of("WorldRegion ", ChatFormatting.BLUE).add(_name).startNewLine(2)
                .add("World: ", ChatFormatting.DARK_GREEN).add(LevelUtils.getName(_world)).startNewLine()
                .add("Members: ", ChatFormatting.LIGHT_PURPLE).startNewLine(4).add(Members.getTextBuilder()).startNewLine(2)
                .add("Flags: ", ChatFormatting.DARK_RED).startNewLine(4).add(Flags.getTextBuilder()).setNextTabLength(0);
    }
}
