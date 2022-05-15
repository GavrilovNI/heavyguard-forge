package me.doggy.heavyguard.api.utils;

import com.mojang.brigadier.Message;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TextBuilder implements Message, ISendable
{
    private int _nextTabLength = -1;
    private List<BaseComponent> _lines = new ArrayList<>();
    private BaseComponent _currentLine;
    
    private TextBuilder()
    {
        startNewLine();
    }
    
    public static TextBuilder of()
    {
        return new TextBuilder();
    }
    public static TextBuilder of(Component text)
    {
        return new TextBuilder().add(text);
    }
    public static TextBuilder of(String string)
    {
        return new TextBuilder().add(string);
    }
    public static TextBuilder of(String string, Style style)
    {
        return new TextBuilder().add(string, style);
    }
    public static TextBuilder of(String string, ChatFormatting formatting)
    {
        return new TextBuilder().add(string, formatting);
    }
    public static TextBuilder of(String string, TextColor textColor)
    {
        return new TextBuilder().add(string, textColor);
    }
    
    private static String getTab(int tabSize)
    {
        if(tabSize < 0)
            tabSize = 0;
        return " ".repeat(tabSize);
    }
    
    public boolean isEmpty()
    {
        return _lines.size() > 1 || _lines.get(0).getSiblings().isEmpty();
    }
    
    public int getLinesCount()
    {
        return _lines.size();
    }
    
    public int getTabSize(int lineIndex)
    {
        return _lines.get(lineIndex).getContents().length();
    }
    
    public int getTabSize()
    {
        return getTabSize(_lines.size() - 1);
    }
    
    public TextBuilder setTabSize(int length)
    {
        if(length < 0)
            length = 0;
        var oldLine = _lines.remove(_lines.size() - 1);
        startNewLine(length);
        for(var text : oldLine.getSiblings())
            _currentLine.append(text);
        return this;
    }
    
    public TextBuilder startNewLine()
    {
        int tabLength;
        if(_nextTabLength < 0)
            tabLength = _lines.isEmpty() ? 0 : getTabSize();
        else
            tabLength = _nextTabLength;
        return startNewLine(tabLength);
    }
    public TextBuilder startNewLine(int tabLength)
    {
        _nextTabLength = -1;
        _currentLine = new TextComponent(TextBuilder.getTab(tabLength));
        _lines.add(_currentLine);
        return this;
    }
    public TextBuilder setNextTabLength(int length)
    {
        _nextTabLength = length;
        return this;
    }
    
    public TextBuilder removeLastLine()
    {
        _lines.remove(_lines.size() - 1);
        if(_lines.isEmpty())
            startNewLine();
        else
            _currentLine = _lines.get(_lines.size() - 1);
        return this;
    }
    private void addSiblings(Component text)
    {
        for(var sibling : text.getSiblings())
            add(sibling);
    }
    public TextBuilder add(TextBuilder textBuilder)
    {
        int oldTabSize = getTabSize();
        
        int firstTextBuilderTabSize = textBuilder.getTabSize(0);
        if(_currentLine.getSiblings().isEmpty())
            setTabSize(oldTabSize + firstTextBuilderTabSize);
        else
            add(new TextComponent(TextBuilder.getTab(firstTextBuilderTabSize)));
        
        addSiblings(textBuilder._lines.get(0).copy());
        
        for(int i = 1; i < textBuilder._lines.size(); i++)
        {
            startNewLine(oldTabSize + textBuilder.getTabSize(i));
            addSiblings(textBuilder._lines.get(i).copy());
        }
        _nextTabLength = textBuilder._nextTabLength;
        return this;
    }
    public TextBuilder add(Component text)
    {
        _currentLine.append(text.copy());
        return this;
    }
    public TextBuilder add(String string, Style style)
    {
        var texts = Component.nullToEmpty(string).toFlatList(style);
        texts.forEach(text -> _currentLine.append(text));
        return this;
    }
    public TextBuilder add(String string, ChatFormatting formatting)
    {
        var texts = Component.nullToEmpty(string).toFlatList(Style.EMPTY.withColor(formatting));
        texts.forEach(text -> _currentLine.append(text));
        return this;
    }
    public TextBuilder add(String string, TextColor color)
    {
        var texts = Component.nullToEmpty(string).toFlatList(Style.EMPTY.withColor(color));
        texts.forEach(text -> _currentLine.append(text));
        return this;
    }
    public TextBuilder add(String string)
    {
        _currentLine.append(Component.nullToEmpty(string));
        return this;
    }
    
    public <T>void send(T object, BiConsumer<T, Component> sendFunc)
    {
        for(var line : _lines)
            sendFunc.accept(object, line);
    }
    
    @Override
    public String toString()
    {
        return toString("\n");
    }
    
    private static String textToString(Component text)
    {
        String result = text.getContents();
        for(var sibling : text.getSiblings())
            result += textToString(sibling);
        return result;
    }
    
    public String toString(String lineDelimiter)
    {
        String result = "";
        for(int i = 0; i < _lines.size(); i++)
        {
            result += textToString(_lines.get(i));
            if(i < _lines.size() - 1)
                result += lineDelimiter;
        }
        return result;
    }
    
    @Override
    public String getString()
    {
        return toString("\n");
    }
}
