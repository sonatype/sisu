package org.codehaus.plexus.test.cycle;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;

public class EagerComponent
    implements Startable
{
    LockComponent lockComponent;

    Thread lockThread;

    public void start()
        throws StartingException
    {
        lockThread = new Thread( new Runnable()
        {
            public void run()
            {
                lockComponent.update();
            }
        } );

        lockThread.start();

        try
        {
            Thread.sleep( 200 );
        }
        catch ( final InterruptedException e )
        {
            // ignore
        }

        lockComponent.update();
    }

    public void stop()
    {

        try
        {
            lockThread.join();
        }
        catch ( final InterruptedException e )
        {
            throw new RuntimeException();
        }
    }
}
