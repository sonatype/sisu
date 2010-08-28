package org.codehaus.plexus.component.registry;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

public class TestSynchronizedComponent
    implements Startable
{

    private Thread lookupThread;

    public synchronized void start()
        throws StartingException
    {
    }

    public synchronized void stop()
        throws StoppingException
    {
        lookupThread.start();
        try
        {
            lookupThread.join();
        }
        catch ( final InterruptedException e )
        {
            throw new StoppingException( "Can't stop lookupThread", e );
        }
    }

    public synchronized void setLookupThread( final Thread lookupThread )
    {
        this.lookupThread = lookupThread;
    }

}
