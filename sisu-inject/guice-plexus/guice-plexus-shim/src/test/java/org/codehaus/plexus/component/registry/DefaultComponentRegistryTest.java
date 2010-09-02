package org.codehaus.plexus.component.registry;

import junit.framework.TestCase;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class DefaultComponentRegistryTest
    extends TestCase
{

    public void testConcurrentDisposeAndLookup()
        throws Exception
    {
        final PlexusContainer plexus = new DefaultPlexusContainer();

        final ComponentDescriptor<TestSynchronizedComponent> descriptor =
            new ComponentDescriptor<TestSynchronizedComponent>( TestSynchronizedComponent.class,
                                                                plexus.getContainerRealm() );
        descriptor.setRole( TestSynchronizedComponent.class.getCanonicalName() );
        descriptor.setImplementation( TestSynchronizedComponent.class.getCanonicalName() );
        plexus.addComponentDescriptor( descriptor );

        final TestSynchronizedComponent component = plexus.lookup( TestSynchronizedComponent.class );

        class LookupThread
            extends Thread
        {
            private TestSynchronizedComponent component;

            @Override
            public synchronized void run()
            {
                try
                {
                    component = plexus.lookup( TestSynchronizedComponent.class );
                }
                catch ( final ComponentLookupException e )
                {
                    // expected
                }
            }

            public synchronized TestSynchronizedComponent getComponent()
            {
                return component;
            }
        }

        final LookupThread lookupThread = new LookupThread();

        component.setLookupThread( lookupThread );

        plexus.dispose();

        assertNull( lookupThread.getComponent() );
    }

}
