package org.codehaus.plexus.test;

import static junit.framework.Assert.assertTrue;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

public class AbstractStartableComponent
    implements StartableComponent, Startable
{
    public int startOrder;

    public int stopOrder;

    public AbstractStartableComponent component1;

    public AbstractStartableComponent component2;

    public void start()
        throws StartingException
    {
        startOrder = startGenerator.getAndIncrement();
    }

    public void stop()
        throws StoppingException
    {
        stopOrder = stopGenerator.getAndIncrement();
    }

    public void assertStartOrderCorrect()
    {
        if ( component1 != null )
        {
            assertTrue( "This component started before injected component1", startOrder > component1.startOrder );
        }
        if ( component2 != null )
        {
            assertTrue( "This component started before injected component2", startOrder > component2.startOrder );
        }

        // assert children are correct
        if ( component1 != null )
        {
            component1.assertStartOrderCorrect();
        }
        if ( component2 != null )
        {
            component2.assertStartOrderCorrect();
        }
    }

    public void assertStopOrderCorrect()
    {
        if ( component1 != null )
        {
            assertTrue( "This component stopped after injected component1", stopOrder < component1.stopOrder );
        }
        if ( component2 != null )
        {
            assertTrue( "This component stopped after injected component2", stopOrder < component2.stopOrder );
        }

        // assert children are correct
        if ( component1 != null )
        {
            component1.assertStopOrderCorrect();
        }
        if ( component2 != null )
        {
            component2.assertStopOrderCorrect();
        }
    }
}
