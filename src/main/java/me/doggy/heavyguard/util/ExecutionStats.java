package me.doggy.heavyguard.util;

public class ExecutionStats
{
    public int success = 0;
    public int failed = 0;
    public int skipped = 0;
    
    public int getTotalTries()
    {
        return success + failed;
    }
    
    public int getTotal()
    {
        return success + failed + skipped;
    }
    
    @Override
    public String toString()
    {
        return success + "/" + failed + "/" + skipped + " (success/failed/skipped)";
    }
}
