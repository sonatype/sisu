package org.codehaus.plexus.test.cycle;

public class DefaultCycleComponentA
    implements CycleComponent
{
    CycleComponent b;

    public CycleComponent next()
    {
        return b;
    }
}
