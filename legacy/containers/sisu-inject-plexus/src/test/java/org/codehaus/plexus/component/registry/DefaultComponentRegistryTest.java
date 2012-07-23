package org.codehaus.plexus.component.registry;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.components.A;

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

    public void testImplementationClassNotFromRealm()
        throws Exception
    {
        final File aJar = new File( "src/test/test-components/component-a-1.0-SNAPSHOT.jar" );
        final ClassLoader customLoader =
            URLClassLoader.newInstance( new URL[] { aJar.toURL() }, getClass().getClassLoader() );
        final Class componentA = customLoader.loadClass( "org.codehaus.plexus.components.DefaultA" );

        final PlexusContainer plexus = new DefaultPlexusContainer();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( A.class.getName() );
        descriptor.setRealm( plexus.getContainerRealm() );
        descriptor.setImplementationClass( componentA );

        plexus.addComponentDescriptor( descriptor );

        assertSame( componentA, plexus.lookup( A.class ).getClass() );

        plexus.dispose();
    }
}
