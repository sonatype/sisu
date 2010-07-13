/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.plexus.locators;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.ClassWorldException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class PlexusBeanLocatorTest
    extends TestCase
{
    @ImplementedBy( BeanImpl.class )
    interface Bean
    {
    }

    static class BeanImpl
        implements Bean
    {
    }

    Injector parent;

    Injector child1;

    Injector child2;

    Injector child3;

    @Override
    public void setUp()
        throws Exception
    {
        parent = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "-" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "Z" ) ).to( BeanImpl.class );
            }
        } );

        child1 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M1" ) ).to( BeanImpl.class );
                final Binder hiddenBinder = binder().withSource( InjectorBeansTest.TEST_HIDDEN_SOURCE );
                hiddenBinder.bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "N1" ) ).to( BeanImpl.class );
            }
        } );

        child2 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
            }
        } );

        child3 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M3" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "N3" ) ).to( BeanImpl.class );
            }
        } );
    }

    public void testInjectorOrdering()
    {
        final DefaultPlexusBeanLocator locator = new DefaultPlexusBeanLocator();

        final Iterable<? extends Entry<String, Bean>> roles = locator.locate( TypeLiteral.get( Bean.class ) );

        locator.add( parent );
        locator.add( child1 );
        locator.add( child2 );
        locator.add( child3 );
        locator.remove( child1 );
        locator.add( child1 );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child2 );
        locator.remove( child2 );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child3 );
        locator.add( child3 );
        locator.add( child3 );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( parent );

        i = roles.iterator();
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child1 );
        locator.remove( child3 );

        i = roles.iterator();
        assertFalse( i.hasNext() );
    }

    public void testExistingInjectors()
    {
        final DefaultPlexusBeanLocator locator = new DefaultPlexusBeanLocator();

        locator.add( parent );
        locator.add( child1 );
        final Iterable<? extends Entry<String, Bean>> roles = locator.locate( TypeLiteral.get( Bean.class ) );
        locator.add( child2 );
        locator.add( child3 );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertFalse( i.hasNext() );
    }

    public void testWeakInjectors()
        throws Exception
    {
        final DefaultPlexusBeanLocator locator = new DefaultPlexusBeanLocator();

        final Field exposedBeansField = DefaultPlexusBeanLocator.class.getDeclaredField( "exposedBeans" );
        exposedBeansField.setAccessible( true );

        final List<?> exposedBeans = (List<?>) exposedBeansField.get( locator );

        assertEquals( 0, exposedBeans.size() );
        Iterable<?> a = locator.locate( TypeLiteral.get( Bean.class ) );
        assertEquals( 1, exposedBeans.size() );
        Iterable<?> b = locator.locate( TypeLiteral.get( Bean.class ) );
        assertEquals( 2, exposedBeans.size() );
        Iterable<?> c = locator.locate( TypeLiteral.get( Bean.class ) );
        assertEquals( 3, exposedBeans.size() );

        a.iterator();
        b.iterator();
        c.iterator();

        forceGC();

        assertEquals( 3, exposedBeans.size() );
        locator.add( child2 );
        assertEquals( 3, exposedBeans.size() );

        b.iterator();
        b = null;
        forceGC();

        assertEquals( 3, exposedBeans.size() );
        locator.remove( child2 );
        assertEquals( 2, exposedBeans.size() );

        a.iterator();
        a = null;
        forceGC();

        assertEquals( 2, exposedBeans.size() );
        locator.add( child2 );
        assertEquals( 1, exposedBeans.size() );

        c.iterator();
        c = null;
        forceGC();

        assertEquals( 1, exposedBeans.size() );
        locator.locate( TypeLiteral.get( Bean.class ) );
        assertEquals( 1, exposedBeans.size() );
    }

    public void testInjectorManagement()
        throws Exception
    {
        final DefaultPlexusBeanLocator locator = new DefaultPlexusBeanLocator();

        final Field injectorsField = DefaultPlexusBeanLocator.class.getDeclaredField( "injectors" );
        injectorsField.setAccessible( true );

        final Set<?> injectors = (Set<?>) injectorsField.get( locator );

        assertEquals( 0, injectors.size() );
        locator.add( null );
        assertEquals( 0, injectors.size() );
        locator.remove( null );
        assertEquals( 0, injectors.size() );
        locator.add( parent );
        assertEquals( 1, injectors.size() );
        locator.add( child1 );
        assertEquals( 2, injectors.size() );
        locator.add( parent );
        assertEquals( 2, injectors.size() );
        locator.remove( parent );
        assertEquals( 1, injectors.size() );
        locator.remove( parent );
        assertEquals( 1, injectors.size() );
    }

    public void testRoleHintLookup()
    {
        final DefaultPlexusBeanLocator locator = new DefaultPlexusBeanLocator();

        final Iterable<? extends Entry<String, Bean>> roles =
            locator.locate( TypeLiteral.get( Bean.class ), "A", "M1", "N3", "-", "!", "-", "M3", "N1", "Z" );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        final Entry<String, Bean> pling = i.next();
        assertEquals( "!", pling.getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        assertEquals( "!=<missing>", pling.toString() );

        try
        {
            pling.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        try
        {
            pling.setValue( null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        locator.add( parent );
        locator.add( child1 );
        locator.add( child2 );
        locator.add( child3 );

        locator.add( parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M1" ) ).toProvider( new Provider<Bean>()
                {
                    public Bean get()
                    {
                        return null;
                    }
                } );
                bind( Bean.class ).annotatedWith( Names.named( "M3" ) ).toProvider( new Provider<Bean>()
                {
                    public Bean get()
                    {
                        return null;
                    }
                } );
            }
        } ) );

        Entry<String, Bean> m1, dash1, dash2, m3;

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        m1 = i.next();
        assertEquals( "M1", m1.getKey() );
        assertEquals( "N3", i.next().getKey() );
        dash1 = i.next();
        assertEquals( "-", dash1.getKey() );
        assertEquals( "!", i.next().getKey() );
        dash2 = i.next();
        assertEquals( "-", dash2.getKey() );
        m3 = i.next();
        assertEquals( "M3", m3.getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        assertNull( m1.getValue() );
        assertSame( dash1.getValue(), dash2.getValue() );
        assertNull( m3.getValue() );

        locator.remove( child1 );
        locator.add( child1 );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        m1 = i.next();
        assertEquals( "M1", m1.getKey() );
        assertEquals( "N3", i.next().getKey() );
        dash1 = i.next();
        assertEquals( "-", dash1.getKey() );
        assertEquals( "!", i.next().getKey() );
        dash2 = i.next();
        assertEquals( "-", dash2.getKey() );
        m3 = i.next();
        assertEquals( "M3", m3.getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        assertEquals( "M1=" + m1.getValue(), m1.toString() );
        assertEquals( BeanImpl.class, m1.getValue().getClass() );
        assertSame( m1.getValue(), m1.getValue() );
        assertSame( dash1.getValue(), dash2.getValue() );
        assertNull( m3.getValue() );
    }

    public void testInjectorVisibility()
        throws NoSuchRealmException
    {
        final DefaultPlexusBeanLocator locator = new DefaultPlexusBeanLocator();
        final ClassWorld world = new ClassWorld();

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.newRealm( "A" ) );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "B" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.newRealm( "B" ) );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "C" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.newRealm( "C" ) );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "B1" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.getRealm( "B" ).createChildRealm( "B1" ) );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "B2" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.getRealm( "B" ).createChildRealm( "B2" ) );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "B3" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.getRealm( "B" ).createChildRealm( "B3" ) );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "B2B" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.getRealm( "B2" ).createChildRealm( "B2B" ) );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "?" ) ).to( BeanImpl.class );
                    bind( ClassRealm.class ).toInstance( world.newRealm( "?" ) );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ) );

        world.getRealm( "B" ).importFrom( "A", "A" );
        world.getRealm( "B" ).importFrom( "C", "C" );

        world.getRealm( "B2" ).importFrom( "B1", "B1" );
        world.getRealm( "B2" ).importFrom( "B3", "B3" );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( BeanImpl.class );
            }
        } ) );

        final Iterable<? extends Entry<String, Bean>> beans = locator.locate( TypeLiteral.get( Bean.class ) );
        Iterator<? extends Entry<String, Bean>> i;

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "A" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "!", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "!", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B2" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B1", i.next().getKey() );
        assertEquals( "B2", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "!", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B2B" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B1", i.next().getKey() );
        assertEquals( "B2", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "B2B", i.next().getKey() );
        assertEquals( "!", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B3" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "!", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "C" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "!", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( null );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B1", i.next().getKey() );
        assertEquals( "B2", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "B2B", i.next().getKey() );
        assertEquals( "?", i.next().getKey() );
        assertEquals( "!", i.next().getKey() );
        assertFalse( i.hasNext() );
    }

    private static String[] forceGC()
    {
        String[] buf;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        return buf;
    }
}
