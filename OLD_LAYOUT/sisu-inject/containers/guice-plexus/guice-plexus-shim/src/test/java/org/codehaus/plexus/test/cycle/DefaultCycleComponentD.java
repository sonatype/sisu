package org.codehaus.plexus.test.cycle;

public class DefaultCycleComponentD
    implements CycleComponent
{
    CycleComponent a;

    public CycleComponent next()
    {
        return a;
    }
}
