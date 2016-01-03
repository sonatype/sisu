package org.codehaus.plexus.test.cycle;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import junit.framework.Assert;

public class DefaultCycleComponentC
    implements CycleComponent, Initializable
{
    CycleComponent d;

    public CycleComponent next()
    {
        return d;
    }

    public void initialize()
    {
        Assert.assertNotNull( next().next().next() );
    }
}
