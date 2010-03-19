/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Named;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Jsr330;

public class GuiceBeanLocatorTest
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
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "-" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "Z" ) ).to( BeanImpl.class );
            }
        } );

        child1 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "M1" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "N1" ) ).to( BeanImpl.class );
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
                bind( Bean.class ).annotatedWith( Jsr330.named( "M3" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "N3" ) ).to( BeanImpl.class );
            }
        } );
    }

    public void testInjectorOrdering()
    {
        final GuiceBeanLocator locator = new GuiceBeanLocator();

        final Iterable<Entry<String, Bean>> roles =
            new NamedIterableAdapter<Bean>( locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) ) );

        locator.add( parent );
        locator.add( child1 );
        locator.add( child2 );
        locator.add( child3 );
        locator.remove( child1 );
        locator.add( child1 );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertNull( i.next().getKey() );
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
        assertNull( i.next().getKey() );
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
        assertNull( i.next().getKey() );
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
        final GuiceBeanLocator locator = new GuiceBeanLocator();

        locator.add( parent );
        locator.add( child1 );

        final Iterable<Entry<String, Bean>> roles =
            new NamedIterableAdapter<Bean>( locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) ) );

        locator.add( child2 );
        locator.add( child3 );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertNull( i.next().getKey() );
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
        final GuiceBeanLocator locator = new GuiceBeanLocator();

        final Field guiceBeansField = GuiceBeanLocator.class.getDeclaredField( "exposedBeans" );
        guiceBeansField.setAccessible( true );

        final List<?> guiceBeans = (List<?>) guiceBeansField.get( locator );

        assertEquals( 0, guiceBeans.size() );
        Iterable<?> a = locator.locate( Key.get( Bean.class ) );
        assertEquals( 1, guiceBeans.size() );
        Iterable<?> b = locator.locate( Key.get( Bean.class ) );
        assertEquals( 2, guiceBeans.size() );
        Iterable<?> c = locator.locate( Key.get( Bean.class ) );
        assertEquals( 3, guiceBeans.size() );

        a.iterator();
        b.iterator();
        c.iterator();

        System.gc();
        System.gc();
        System.gc();

        assertEquals( 3, guiceBeans.size() );
        locator.add( child2 );
        assertEquals( 3, guiceBeans.size() );

        b.iterator();
        b = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 3, guiceBeans.size() );
        locator.remove( child2 );
        assertEquals( 2, guiceBeans.size() );

        a.iterator();
        a = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 2, guiceBeans.size() );
        locator.add( child2 );
        assertEquals( 1, guiceBeans.size() );

        c.iterator();
        c = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 1, guiceBeans.size() );
        locator.locate( Key.get( Bean.class ) );
        assertEquals( 1, guiceBeans.size() );
    }

    public void testInjectorManagement()
        throws Exception
    {
        final GuiceBeanLocator locator = new GuiceBeanLocator();

        final Field injectorsField = GuiceBeanLocator.class.getDeclaredField( "injectors" );
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

    static class CountingWatcher
        implements Watcher<Entry<Annotation, Bean>>
    {
        Set<Entry<Annotation, Bean>> entries = new HashSet<Entry<Annotation, Bean>>();

        public void add( final Entry<Annotation, Bean> item )
        {
            assertTrue( entries.add( item ) );
        }

        public void remove( final Entry<Annotation, Bean> item )
        {
            assertTrue( entries.remove( item ) );
        }
    }

    public void testWatchableSequence()
    {
        final GuiceBeanLocator locator = new GuiceBeanLocator();

        final Watchable<Entry<Annotation, Bean>> watchable = locator.locate( Key.get( Bean.class ) );
        final CountingWatcher countingWatcher = new CountingWatcher();

        assertNull( watchable.subscribe( countingWatcher ) );
        assertNull( watchable.subscribe( countingWatcher ) );
        watchable.unsubscribe( countingWatcher );
        watchable.unsubscribe( countingWatcher );

        locator.add( null );
        locator.remove( null );

        locator.add( parent );
        locator.add( child1 );

        assertNull( watchable.subscribe( countingWatcher ) );
        assertEquals( 6, countingWatcher.entries.size() );

        locator.add( child2 );
        assertEquals( 6, countingWatcher.entries.size() );
        locator.add( child3 );
        assertEquals( 8, countingWatcher.entries.size() );
        locator.add( child2 );
        assertEquals( 8, countingWatcher.entries.size() );
        locator.add( child1 );
        assertEquals( 8, countingWatcher.entries.size() );
        locator.add( parent );
        assertEquals( 8, countingWatcher.entries.size() );

        locator.remove( child1 );
        assertEquals( 6, countingWatcher.entries.size() );
        locator.remove( parent );
        assertEquals( 2, countingWatcher.entries.size() );
        locator.remove( child3 );
        assertEquals( 0, countingWatcher.entries.size() );
        locator.remove( parent );
        assertEquals( 0, countingWatcher.entries.size() );
        locator.remove( child2 );
        assertEquals( 0, countingWatcher.entries.size() );
        locator.remove( parent );
        assertEquals( 0, countingWatcher.entries.size() );

        locator.add( child3 );
        assertEquals( 2, countingWatcher.entries.size() );
        assertSame( countingWatcher, watchable.subscribe( null ) );
        locator.remove( child3 );
        assertEquals( 2, countingWatcher.entries.size() );
    }
}
