package org.codehaus.plexus.test;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.discovery.DiscoveredComponent;
import org.codehaus.plexus.test.list.Pipeline;
import org.codehaus.plexus.test.list.Valve;
import org.codehaus.plexus.test.list.ValveFour;
import org.codehaus.plexus.test.list.ValveOne;
import org.codehaus.plexus.test.list.ValveThree;
import org.codehaus.plexus.test.list.ValveTwo;
import org.codehaus.plexus.test.map.Activity;
import org.codehaus.plexus.test.map.ActivityManager;

public class PlexusContainerTest
    extends TestCase
{
    private String basedir;

    private ClassLoader classLoader;

    private String configuration;

    private DefaultPlexusContainer container;

    public PlexusContainerTest( final String name )
    {
        super( name );
    }

    @Override
    public void setUp()
        throws Exception
    {
        basedir = System.getProperty( "basedir" );

        classLoader = getClass().getClassLoader();

        configuration = "/" + getClass().getName().replace( '.', '/' ) + ".xml";

        assertNotNull( classLoader );

        // ----------------------------------------------------------------------------
        // Context
        // ----------------------------------------------------------------------------

        final Map<Object, Object> context = new HashMap<Object, Object>();

        context.put( "basedir", basedir );

        context.put( "plexus.home", basedir + "/target/plexus-home" );

        final ContainerConfiguration containerConfiguration =
            new DefaultContainerConfiguration().setName( "test" ).setContext( context ).setContainerConfiguration( configuration );

        container = new DefaultPlexusContainer( containerConfiguration );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        container.dispose();

        container = null;
    }

    // ----------------------------------------------------------------------
    // Test the native plexus lifecycle. Note that the configuration for
    // this TestCase supplies its own lifecycle, so this test verifies that
    // the native lifecycle is available after configuration merging.
    // ----------------------------------------------------------------------

    public void testNativeLifecyclePassage()
        throws Exception
    {
        final DefaultServiceB serviceB = (DefaultServiceB) container.lookup( ServiceB.class );

        // Make sure the component is alive.
        assertNotNull( serviceB );

        // Make sure the component went through all the lifecycle phases
        assertEquals( true, serviceB.enableLogging );

        assertEquals( true, serviceB.contextualize );

        assertEquals( true, serviceB.initialize );

        assertEquals( true, serviceB.start );

        assertEquals( false, serviceB.stop );

        container.release( serviceB );

        assertEquals( true, serviceB.stop );
    }

    public void testConfigurableLifecyclePassage()
        throws Exception
    {
        final DefaultServiceE serviceE = (DefaultServiceE) container.lookup( ServiceE.class );

        // Make sure the component is alive.
        assertNotNull( serviceE );

        // Make sure the component went through all the lifecycle phases
        assertEquals( true, serviceE.enableLogging );

        assertEquals( true, serviceE.contextualize );

        assertEquals( true, serviceE.initialize );

        assertEquals( true, serviceE.start );

        assertEquals( false, serviceE.stop );

        container.release( serviceE );

        assertEquals( true, serviceE.stop );
    }

    /*
     * Check that we can get references to a single component with a role hint.
     */
    public void testSingleComponentLookupWithRoleHint()
        throws Exception
    {
        // Retrieve an instance of component c.
        final DefaultServiceC serviceC1 = (DefaultServiceC) container.lookup( ServiceC.class, "first-instance" );

        // Make sure the component is alive.
        assertNotNull( serviceC1 );

        assertTrue( serviceC1.started );

        assertFalse( serviceC1.stopped );

        // Retrieve a second reference to the same component.
        final DefaultServiceC serviceC2 = (DefaultServiceC) container.lookup( ServiceC.class, "first-instance" );

        // Make sure component is alive.
        assertNotNull( serviceC2 );

        assertTrue( serviceC2.started );

        assertFalse( serviceC2.stopped );

        // Let's make sure it gave us back the same component.
        assertSame( serviceC1, serviceC2 );

        container.release( serviceC1 );

        // The component should still be alive.
        assertTrue( serviceC2.started );

        assertTrue( serviceC2.stopped );

        container.release( serviceC2 );

        // The component should now have been stopped.
        assertTrue( serviceC2.started );

        assertTrue( serviceC2.stopped );
    }

    /*
     * Check that distinct components with the same implementation are managed correctly.
     */
    public void testMultipleSingletonComponentInstances()
        throws Exception
    {
        // Retrieve an instance of component c.
        final DefaultServiceC serviceC1 = (DefaultServiceC) container.lookup( ServiceC.class, "first-instance" );

        // Make sure the component is alive.
        assertNotNull( serviceC1 );

        assertTrue( serviceC1.started );

        assertFalse( serviceC1.stopped );

        // Retrieve an instance of component c, with a different role hint.
        // This should give us a different component instance.
        final DefaultServiceC serviceC2 = (DefaultServiceC) container.lookup( ServiceC.class, "second-instance" );

        // Make sure component is alive.
        assertNotNull( serviceC2 );

        assertTrue( serviceC2.started );

        assertFalse( serviceC2.stopped );

        // The components should be distinct.
        assertNotSame( serviceC1, serviceC2 );

        container.release( serviceC1 );

        // The first component should now have been stopped, the second
        // one should still be alive.
        assertTrue( serviceC1.started );

        assertTrue( serviceC1.stopped );

        assertTrue( serviceC2.started );

        assertFalse( serviceC2.stopped );

        container.release( serviceC2 );

        // The second component should now have been stopped.
        assertTrue( serviceC2.started );

        assertTrue( serviceC2.stopped );
    }

    public void testLookupAll()
        throws Exception
    {
        final Map<String, ServiceC> components = container.lookupMap( ServiceC.class );

        assertNotNull( components );

        assertEquals( 2, components.size() );

        ServiceC component = components.get( "first-instance" );

        assertNotNull( component );

        component = components.get( "second-instance" );

        assertNotNull( component );

        container.releaseAll( components );
    }

    public void testAutomatedComponentConfigurationUsingXStreamPoweredComponentConfigurator()
        throws Exception
    {
        final Component component = container.lookup( Component.class );

        assertNotNull( component );

        assertNotNull( component.getActivity() );

        assertEquals( "localhost", component.getHost() );

        assertEquals( 10000, component.getPort() );
    }

    public void testAutomatedComponentComposition()
        throws Exception
    {
        final ComponentA componentA = container.lookup( ComponentA.class );

        assertNotNull( componentA );

        assertEquals( "localhost", componentA.getHost() );

        assertEquals( 10000, componentA.getPort() );

        final ComponentB componentB = componentA.getComponentB();

        assertNotNull( componentB );

        final ComponentC componentC = componentA.getComponentC();

        assertNotNull( componentC );

        final ComponentD componentD = componentC.getComponentD();

        assertNotNull( componentD );

        assertEquals( "jason", componentD.getName() );
    }

    public void testComponentCompositionWhereTargetFieldIsAMap()
        throws Exception
    {
        final ActivityManager am = container.lookup( ActivityManager.class );

        final Activity one = am.getActivity( "one" );

        assertNotNull( one );

        // repeated retrieval from map should not cause re-lookup even if instantiation strategy is per-lookup
        assertSame( one, am.getActivity( "one" ) );

        assertFalse( one.getState() );

        am.execute( "one" );

        assertTrue( one.getState() );

        final Activity two = am.getActivity( "two" );

        assertNotNull( two );

        assertFalse( two.getState() );

        am.execute( "two" );

        assertTrue( two.getState() );
    }

    public void testComponentCompositionWhereTargetFieldIsAPartialMap()
        throws Exception
    {
        final ActivityManager am = container.lookup( ActivityManager.class, "slim" );

        assertEquals( 1, am.getActivityCount() );

        final Activity one = am.getActivity( "one" );

        assertNotNull( one );

        assertFalse( one.getState() );

        am.execute( "one" );

        assertTrue( one.getState() );
    }

    public void testComponentCompositionWhereTargetFieldIsAList()
        throws Exception
    {
        final Pipeline pipeline = container.lookup( Pipeline.class );

        final List valves = pipeline.getValves();

        for ( int i = 0; i < valves.size(); i++ )
        {
            // repeated retrieval from list should not cause re-lookup even if instantiation strategy is per-lookup
            assertSame( valves.get( i ), valves.get( i ) );
        }

        assertFalse( ( (Valve) valves.get( 0 ) ).getState() );

        assertFalse( ( (Valve) valves.get( 1 ) ).getState() );

        pipeline.execute();

        assertTrue( ( (Valve) valves.get( 0 ) ).getState() );

        assertTrue( ( (Valve) valves.get( 1 ) ).getState() );
    }

    public void testComponentCompositionWhereTargetFieldIsAPartialList()
        throws Exception
    {
        final Pipeline pipeline = container.lookup( Pipeline.class, "slim" );

        final List valves = pipeline.getValves();

        assertEquals( valves.size(), 1 );

        assertFalse( ( (Valve) valves.get( 0 ) ).getState() );

        pipeline.execute();

        assertTrue( ( (Valve) valves.get( 0 ) ).getState() );
    }

    public void testComponentCompositionWhereTargetFieldAMapThatMustRetainTheOrderOfComponentsGivenASetOfRoleHints()
        throws Exception
    {
        final Pipeline pipeline = container.lookup( Pipeline.class, "chubby" );

        final Map valveMap = pipeline.getValveMap();

        final List valves = new ArrayList( valveMap.values() );

        assertEquals( "Expecting three valves.", 4, valves.size() );

        assertTrue( "Expecting valve one.", valves.get( 0 ) instanceof ValveOne );

        assertTrue( "Expecting valve two.", valves.get( 1 ) instanceof ValveTwo );

        assertTrue( "Expecting valve three.", valves.get( 2 ) instanceof ValveThree );

        assertTrue( "Expecting valve four.", valves.get( 3 ) instanceof ValveFour );
    }

    public void testLookupOfComponentThatShouldBeDiscovered()
        throws Exception
    {
        final DiscoveredComponent discoveredComponent = container.lookup( DiscoveredComponent.class );

        assertNotNull( discoveredComponent );
    }

    public void testStartableComponentSnake()
        throws Exception
    {
        final StartableComponent ca = container.lookup( StartableComponent.class, "A-snake" );

        assertNotNull( ca );

        ca.assertStartOrderCorrect();

        container.dispose();

        ca.assertStopOrderCorrect();
    }

    public void testStartableComponentTree()
        throws Exception
    {
        final StartableComponent ca = container.lookup( StartableComponent.class, "A-tree" );

        assertNotNull( ca );

        ca.assertStartOrderCorrect();

        container.dispose();

        ca.assertStopOrderCorrect();
    }

    public void testAddComponent()
        throws Exception
    {
        final LiveComponent live = new LiveComponent();

        container.addComponent( live, LiveComponent.class.getName() );

        final LiveComponent c = container.lookup( LiveComponent.class );

        assertSame( live, c );
    }

    public void testComponentOverride()
        throws Exception
    {
        assertNotNull( container.lookup( Component.class ) );

        final Component live = new Component()
        {
            public Activity getActivity()
            {
                return null;
            }

            public String getHost()
            {
                return null;
            }

            public int getPort()
            {
                return 0;
            }
        };

        container.addComponent( live, Component.class, null );

        assertSame( live, container.lookup( Component.class ) );
    }

    public void testUpdateOfActiveComponentCollectionUponChangeOfThreadContextClassLoader()
        throws Exception
    {
        final ComponentManager manager = container.lookup( ComponentManager.class );

        Map<String, ?> map = manager.getMap();
        assertNotNull( map );
        assertEquals( 0, map.size() );

        List<?> list = manager.getList();
        assertNotNull( list );
        assertEquals( 0, list.size() );

        /*
         * Below we're creating two realms which basically contain the same components, only their bytecode/version
         * differs. When we switch the thread's context class loader, the active component collections in the component
         * manager must accurately reflect the components from the current realm (and not from a previous realm).
         */

        final ClassRealm realmA = container.createChildRealm( "realm-a" );
        realmA.addURL( new File( "src/test/test-components/component-a-1.0-SNAPSHOT.jar" ).toURI().toURL() );
        container.discoverComponents( realmA );

        final ClassRealm realmB = container.createChildRealm( "realm-b" );
        realmB.addURL( new File( "src/test/test-components/component-a-2.0-SNAPSHOT.jar" ).toURI().toURL() );
        container.discoverComponents( realmB );

        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader( realmA );

            map = manager.getMap();
            assertNotNull( map );
            assertEquals( 1, map.size() );
            assertSame( realmA, map.values().iterator().next().getClass().getClassLoader() );

            list = manager.getList();
            assertNotNull( list );
            assertEquals( 1, list.size() );
            assertSame( realmA, list.iterator().next().getClass().getClassLoader() );

            Thread.currentThread().setContextClassLoader( realmB );

            map = manager.getMap();
            assertNotNull( map );
            assertEquals( 1, map.size() );
            assertSame( realmB, map.values().iterator().next().getClass().getClassLoader() );

            list = manager.getList();
            assertNotNull( list );
            assertEquals( 1, list.size() );
            assertSame( realmB, list.iterator().next().getClass().getClassLoader() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldClassLoader );
        }
    }

    public void testUpdateOfActiveComponentCollectionUponChangeOfThreadContextClassLoaderFromParentToChildRealm()
        throws Exception
    {
        final ComponentManager manager = container.lookup( ComponentManager.class );

        Map<String, ?> map = manager.getMap();
        assertNotNull( map );
        assertEquals( 0, map.size() );

        List<?> list = manager.getList();
        assertNotNull( list );
        assertEquals( 0, list.size() );

        /*
         * Below we're creating two realms which basically contain the same components, only their bytecode/version
         * differs. The realms form a parent-child relationship where the child imports the component role from the
         * parent. When we first load from the parent and then switch the thread's context class loader to the child,
         * the active component collections in the component manager must accurately reflect the components from the
         * current realm (and not from a previous realm).
         */

        final ClassRealm realmA = container.createChildRealm( "realm-a" );
        realmA.addURL( new File( "src/test/test-components/component-a-1.0-SNAPSHOT.jar" ).toURI().toURL() );
        container.discoverComponents( realmA );

        final ClassRealm realmB = realmA.createChildRealm( "realm-b" );
        realmB.importFrom( realmA, "org.codehaus.plexus.components.A" );
        realmB.importFromParent( "nothing" );
        realmB.addURL( new File( "src/test/test-components/component-a-2.0-SNAPSHOT.jar" ).toURI().toURL() );
        container.discoverComponents( realmB );

        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader( realmA );

            map = manager.getMap();
            assertNotNull( map );
            assertEquals( 1, map.size() );
            assertSame( realmA, map.values().iterator().next().getClass().getClassLoader() );

            list = manager.getList();
            assertNotNull( list );
            assertEquals( 1, list.size() );
            assertSame( realmA, list.iterator().next().getClass().getClassLoader() );

            Thread.currentThread().setContextClassLoader( realmB );

            map = manager.getMap();
            assertNotNull( map );
            assertEquals( 2, map.size() );
            assertSame( realmB, map.values().iterator().next().getClass().getClassLoader() );

            list = manager.getList();
            assertNotNull( list );
            assertEquals( 2, list.size() );
            assertSame( realmB, list.iterator().next().getClass().getClassLoader() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldClassLoader );
        }
    }

    public void testComponentLookupFromParentRealmOfImportedRealms()
        throws Exception
    {
        final ComponentManager manager = container.lookup( ComponentManager.class );

        Map<String, ?> map = manager.getMap();
        assertNotNull( map );
        assertEquals( 0, map.size() );

        List<?> list = manager.getList();
        assertNotNull( list );
        assertEquals( 0, list.size() );

        final URL componentUrl = new File( "src/test/test-components/component-a-1.0-SNAPSHOT.jar" ).toURI().toURL();

        final ClassRealm realmP = container.createChildRealm( "parent-of-imported-realm" );
        realmP.addURL( componentUrl );
        container.discoverComponents( realmP );

        final ClassRealm realmI = realmP.createChildRealm( "imported-realm" );

        final ClassRealm realmL = container.createChildRealm( "lookup-realm" );
        realmL.importFrom( realmI, "org.something" );

        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader( realmL );

            map = manager.getMap();
            assertNotNull( map );
            assertEquals( 1, map.size() );
            assertSame( realmP, map.values().iterator().next().getClass().getClassLoader() );

            list = manager.getList();
            assertNotNull( list );
            assertEquals( 1, list.size() );
            assertSame( realmP, list.iterator().next().getClass().getClassLoader() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldClassLoader );
        }
    }

    public void testOptionalComponentRequirement()
        throws Exception
    {
        final DefaultComponentWithOptionalRequirement ca =
            (DefaultComponentWithOptionalRequirement) container.lookup( Component.class, "with-optional" );

        assertNotNull( ca );

        assertNotNull( ca.getActivity() );

        assertNull( ca.optionalComponent );
    }

    public void testLookupOfComponentThatHasARequirementWithoutRoleHintAndTheOneAndOnlyImplHasNoDefaultHint()
        throws Exception
    {
        final DefaultThingUser component = (DefaultThingUser) container.lookup( ThingUser.class );

        assertNotNull( component.thing );
    }

    public void testSingleLookupWithAndWithoutRoleHint()
        throws Exception
    {
        final ComponentWithRoleDefault withRoleHint = container.lookup( ComponentWithRoleDefault.class, "default" );

        final ComponentWithRoleDefault withoutRoleHint = container.lookup( ComponentWithRoleDefault.class );

        assertSame( withRoleHint, withoutRoleHint );
    }

    public void testLookupUponChangeOfThreadContextClassLoaderFromParentToChildRealm()
        throws Exception
    {
        /*
         * Below we're creating two realms which basically contain the same components, only their bytecode/version
         * differs. The realms form a parent-child relationship where the child imports the component role from the
         * parent. When we first lookup from the parent and then switch the thread's context class loader to the child,
         * the second lookup must accurately reflect the components from the current realm (and not from a previous
         * realm).
         */

        final ClassRealm realmA = container.createChildRealm( "realm-a" );
        realmA.addURL( new File( "src/test/test-components/component-a-1.0-SNAPSHOT.jar" ).toURI().toURL() );
        container.discoverComponents( realmA );

        final ClassRealm realmB = realmA.createChildRealm( "realm-b" );
        realmB.importFrom( realmA, "org.codehaus.plexus.components.A" );
        realmB.importFromParent( "nothing" );
        realmB.addURL( new File( "src/test/test-components/component-a-2.0-SNAPSHOT.jar" ).toURI().toURL() );
        container.discoverComponents( realmB );

        final Class<?> role = realmA.loadClass( "org.codehaus.plexus.components.A" );

        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader( realmA );

            final Object comp1 = container.lookup( role, "default" );

            Thread.currentThread().setContextClassLoader( realmB );

            final Object comp2 = container.lookup( role, "default" );

            assertNotNull( comp1 );
            assertNotNull( comp2 );
            assertNotSame( comp1, comp2 );
            assertSame( realmA, comp1.getClass().getClassLoader() );
            assertSame( realmB, comp2.getClass().getClassLoader() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldClassLoader );
        }
    }

    public void testSafeConcurrentAccessToActiveComponentCollection()
        throws Exception
    {
        final ComponentManager manager = container.lookup( ComponentManager.class );

        final Map<String, ?> map = manager.getMap();
        assertNotNull( map );
        assertEquals( 0, map.size() );

        final List<?> list = manager.getList();
        assertNotNull( list );
        assertEquals( 0, list.size() );

        final AtomicBoolean go = new AtomicBoolean( false );

        final List<Exception> exceptions = new CopyOnWriteArrayList<Exception>();
        final Thread[] threads = new Thread[64];
        final CountDownLatch latch = new CountDownLatch( threads.length );
        for ( int i = 0; i < threads.length; i++ )
        {
            threads[i] = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final ClassRealm realm = container.createChildRealm( "realm-" + UUID.randomUUID().toString() );
                        realm.addURL( new File( "src/test/test-components/component-a-1.0-SNAPSHOT.jar" ).toURI().toURL() );
                        container.discoverComponents( realm );
                        Thread.currentThread().setContextClassLoader( realm );

                        while ( !go.get() )
                        {
                            // just wait
                        }

                        for ( int j = 0; j < 1000; j++ )
                        {
                            // this just must not die with some exception
                            for ( final Object value : map.values() )
                            {
                                value.toString();
                            }
                            for ( final Object value : list )
                            {
                                value.toString();
                            }
                        }
                    }
                    catch ( final Exception e )
                    {
                        e.printStackTrace();
                        exceptions.add( e );
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }
            };
            threads[i].start();
        }
        go.set( true );
        latch.await();

        assertTrue( exceptions.toString(), exceptions.isEmpty() );
    }

}
