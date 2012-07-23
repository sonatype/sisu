package org.codehaus.plexus.test.cycle;

public class DefaultCycleComponentB
    implements CycleComponent
{
    CycleComponent c;

    public CycleComponent next()
    {
        return c;
    }
}
