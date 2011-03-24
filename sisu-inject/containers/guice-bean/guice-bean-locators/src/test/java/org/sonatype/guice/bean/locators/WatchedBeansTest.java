/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import java.util.Arrays;

import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.LocatedBeansTest.Marked;
import org.sonatype.guice.bean.locators.RankedBindingsTest.Bean;
import org.sonatype.guice.bean.locators.RankedBindingsTest.BeanImpl;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.locators.spi.BindingSubscriber;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class WatchedBeansTest
    extends TestCase
{
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
                bind( Bean.class ).annotatedWith( Names.named( "B" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "C" ) ).to( BeanImpl.class );
            }
        } );

        child1 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "X" ) ).to( BeanImpl.class );
            }
        } );

        child2 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "Y" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Marked.class ).to( BeanImpl.class );
            }
        } );

        child3 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "Z" ) ).to( BeanImpl.class );
            }
        } );
    }

    static class RankingMediator
        implements Mediator<Named, Bean, RankedList<String>>
    {
        public void add( final BeanEntry<Named, Bean> entry, final RankedList<String> names )
        {
            names.insert( entry.getKey().value(), entry.getRank() );
        }

        public void remove( final BeanEntry<Named, Bean> entry, final RankedList<String> names )
        {
            final int index = names.indexOfThis( entry.getKey().value() );

            final String name = names.get( index );
            final int rank = names.getRank( index );

            assertSame( name, entry.getKey().value() );
            assertEquals( rank, entry.getRank() );

            names.remove( index );
        }
    }

    @SuppressWarnings( "deprecation" )
    public void testWatchedBeans()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();
        RankedList<String> names = new RankedList<String>();

        locator.watch( Key.get( Bean.class, Named.class ), new RankingMediator(), names );

        assertTrue( names.isEmpty() );

        locator.add( parent, 0 );

        assertTrue( Arrays.equals( new Object[] { "A", "B", "C" }, names.toArray() ) );

        locator.add( child1, 1 );

        assertTrue( Arrays.equals( new Object[] { "X", "A", "B", "C" }, names.toArray() ) );

        final BindingSubscriber[] subscriberHolder = new BindingSubscriber[1];
        final BindingPublisher subscriberHook = new BindingPublisher()
        {
            public <T> void subscribe( final TypeLiteral<T> type, final BindingSubscriber subscriber )
            {
                subscriberHolder[0] = subscriber;
            }

            public <T> boolean contains( final Binding<T> binding )
            {
                return false;
            }

            public <T> void unsubscribe( final TypeLiteral<T> type, final BindingSubscriber subscriber )
            {
                subscriberHolder[0] = null;
            }
        };

        locator.add( subscriberHook, Integer.MIN_VALUE );
        assertNotNull( subscriberHolder[0] );

        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ), Integer.MIN_VALUE );
        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Marked.class ) ), Integer.MIN_VALUE );

        assertTrue( Arrays.equals( new Object[] { "X", "A", "B", "C", "Y" }, names.toArray() ) );

        locator.remove( parent );

        assertTrue( Arrays.equals( new Object[] { "X", "Y" }, names.toArray() ) );

        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ) );
        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Marked.class ) ) );

        locator.remove( subscriberHook );
        assertNull( subscriberHolder[0] );

        assertTrue( Arrays.equals( new Object[] { "X" }, names.toArray() ) );

        locator.add( child1, 42 );

        assertTrue( Arrays.equals( new Object[] { "X" }, names.toArray() ) );

        locator.remove( subscriberHook );

        assertTrue( Arrays.equals( new Object[] { "X" }, names.toArray() ) );

        locator.add( child3, 3 );

        assertTrue( Arrays.equals( new Object[] { "Z", "X" }, names.toArray() ) );

        locator.add( parent, 2 );

        assertTrue( Arrays.equals( new Object[] { "Z", "A", "B", "C", "X" }, names.toArray() ) );

        locator.clear();

        assertTrue( Arrays.equals( new Object[0], names.toArray() ) );

        names = null;
        System.gc();

        locator.add( parent, Integer.MAX_VALUE );
        locator.remove( parent );
    }

    static class BrokenMediator
        implements Mediator<Named, Bean, Object>
    {
        public void add( final BeanEntry<Named, Bean> entry, final Object watcher )
        {
            throw new LinkageError();
        }

        public void remove( final BeanEntry<Named, Bean> entry, final Object watcher )
        {
            throw new LinkageError();
        }
    }

    @SuppressWarnings( "deprecation" )
    public void testBrokenWatcher()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        Object keepAlive = new Object();

        locator.add( parent, 0 );
        locator.watch( Key.get( Bean.class, Named.class ), new BrokenMediator(), keepAlive );
        locator.remove( parent );

        final BindingSubscriber[] subscriberHolder = new BindingSubscriber[1];
        final BindingPublisher subscriberHook = new BindingPublisher()
        {
            public <T> void subscribe( final TypeLiteral<T> type, final BindingSubscriber subscriber )
            {
                subscriberHolder[0] = subscriber;
            }

            public <T> boolean contains( final Binding<T> binding )
            {
                return false;
            }

            public <T> void unsubscribe( final TypeLiteral<T> type, final BindingSubscriber subscriber )
            {
                subscriberHolder[0] = null;
            }
        };

        locator.add( subscriberHook, 0 );

        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ), Integer.MIN_VALUE );
        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Marked.class ) ), Integer.MIN_VALUE );

        keepAlive = null;
        System.gc();

        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ) );
        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Marked.class ) ) );
    }
}
