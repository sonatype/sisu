package org.codehaus.plexus.test.cycle;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import junit.framework.Assert;

public class DefaultCycleComponentC
    implements CycleComponent, Initializable
{
    CycleComponent d;

    boolean initialized;

    public CycleComponent next()
    {
        Assert.assertTrue( initialized ); // verify initialized before use

        return d;
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            Thread.sleep( 500 ); // simulate some non-trivial initialization
        }
        catch ( InterruptedException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }

        initialized = true;

        Assert.assertNotNull( next().next().next() ); // exercise the cycle
    }
}
