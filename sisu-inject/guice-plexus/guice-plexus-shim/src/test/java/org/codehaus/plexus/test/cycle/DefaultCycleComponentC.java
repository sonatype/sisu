package org.codehaus.plexus.test.cycle;

import junit.framework.Assert;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

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
