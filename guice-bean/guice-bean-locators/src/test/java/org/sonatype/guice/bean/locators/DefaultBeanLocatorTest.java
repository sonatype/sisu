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

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Named;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Jsr330;

public class DefaultBeanLocatorTest
    extends TestCase
{
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

    public void testDefaultLocator()
    {
        final BeanLocator locator = parent.getInstance( BeanLocator.class );
        assertSame( locator, parent.getInstance( MutableBeanLocator.class ) );

        final Iterable<Entry<String, Bean>> roles =
            new NamedIterableAdapter<Bean>( locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) ) );

        final Iterator<? extends Entry<String, Bean>> i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );
    }

    public void testInjectorOrdering()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

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
        final MutableBeanLocator locator = new DefaultBeanLocator();

        locator.add( parent );
        locator.add( child1 );

        final Iterable<Entry<String, Bean>> roles =
            new NamedIterableAdapter<Bean>( locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) ) );

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

    public void testWeakSequences()
        throws Exception
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Field exposedBeansField = DefaultBeanLocator.class.getDeclaredField( "exposedBeans" );
        exposedBeansField.setAccessible( true );

        final List<?> exposedBeans = (List<?>) exposedBeansField.get( locator );

        assertEquals( 0, exposedBeans.size() );
        Iterable<?> a = locator.locate( Key.get( Bean.class ) );
        assertEquals( 1, exposedBeans.size() );
        Iterable<?> b = locator.locate( Key.get( Bean.class ) );
        assertEquals( 2, exposedBeans.size() );
        Iterable<?> c = locator.locate( Key.get( Bean.class ) );
        assertEquals( 3, exposedBeans.size() );

        a.iterator();
        b.iterator();
        c.iterator();

        System.gc();
        System.gc();
        System.gc();

        assertEquals( 3, exposedBeans.size() );
        locator.add( child2 );
        assertEquals( 3, exposedBeans.size() );

        b.iterator();
        b = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 3, exposedBeans.size() );
        locator.remove( child2 );
        assertEquals( 2, exposedBeans.size() );

        a.iterator();
        a = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 2, exposedBeans.size() );
        locator.add( child2 );
        assertEquals( 1, exposedBeans.size() );

        c.iterator();
        c = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 1, exposedBeans.size() );
        locator.locate( Key.get( Bean.class ) );
        assertEquals( 1, exposedBeans.size() );
    }

    public void testInjectorManagement()
        throws Exception
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Field injectorsField = DefaultBeanLocator.class.getDeclaredField( "injectors" );
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

    static class NullMediator
        implements Mediator<Named, Bean, Object>
    {
        public void add( final Entry<Named, Bean> bean, final Object watcher )
            throws Exception
        {
        }

        public void remove( final Entry<Named, Bean> bean, final Object watcher )
            throws Exception
        {
        }
    }

    static class BrokenMediator
        implements Mediator<Named, Bean, Object>
    {
        public void add( final Entry<Named, Bean> bean, final Object watcher )
            throws Exception
        {
            throw new Exception();
        }

        public void remove( final Entry<Named, Bean> bean, final Object watcher )
            throws Exception
        {
            throw new Exception();
        }
    }

    public void testWeakMediation()
        throws Exception
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Field mediatedWatchersField = DefaultBeanLocator.class.getDeclaredField( "mediatedWatchers" );
        mediatedWatchersField.setAccessible( true );

        final List<?> mediatedWatchers = (List<?>) mediatedWatchersField.get( locator );

        final Mediator<Named, Bean, Object> nullMediator = new NullMediator();
        final Mediator<Named, Bean, Object> brokenMediator = new BrokenMediator();

        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        assertEquals( 0, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), nullMediator, a );
        assertEquals( 1, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), nullMediator, b );
        assertEquals( 2, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), brokenMediator, c );
        assertEquals( 3, mediatedWatchers.size() );

        System.gc();
        System.gc();
        System.gc();

        assertEquals( 3, mediatedWatchers.size() );
        locator.add( child1 );
        assertEquals( 3, mediatedWatchers.size() );

        b = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 3, mediatedWatchers.size() );
        locator.remove( child1 );
        assertEquals( 2, mediatedWatchers.size() );

        a = null;
        System.gc();
        System.gc();
        System.gc();

        assertEquals( 2, mediatedWatchers.size() );
        locator.add( child2 );
        assertEquals( 1, mediatedWatchers.size() );

        c = null;
        System.gc();
        System.gc();
        System.gc();

        a = new Object();

        assertEquals( 1, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), nullMediator, a );
        assertEquals( 1, mediatedWatchers.size() );
    }
}
