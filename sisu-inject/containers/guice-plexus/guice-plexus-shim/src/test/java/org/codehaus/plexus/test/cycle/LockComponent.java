package org.codehaus.plexus.test.cycle;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class LockComponent
{
    ReadWriteLock lock = new ReentrantReadWriteLock();

    PlexusContainer container;

    public void update()
    {
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try
        {
            container.lookup( LazyComponent.class );
        }
        catch ( final ComponentLookupException e )
        {
            throw new RuntimeException( e.toString() );
        }
        finally
        {
            writeLock.unlock();
        }
    }
}
