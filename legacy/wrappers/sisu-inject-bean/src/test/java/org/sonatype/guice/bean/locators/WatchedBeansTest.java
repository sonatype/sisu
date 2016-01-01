/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.DefaultBeanLocatorTest.Bean;
import org.sonatype.guice.bean.locators.DefaultBeanLocatorTest.BeanImpl;
import org.sonatype.guice.bean.locators.DefaultBeanLocatorTest.Marked;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
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
        implements Mediator<Named, Bean, List<String>>
    {
        public void add( final BeanEntry<Named, Bean> entry, final List<String> names )
        {
            names.add( entry.getKey().value() );
        }

        public void remove( final BeanEntry<Named, Bean> entry, final List<String> names )
        {
            assertTrue( names.remove( entry.getKey().value() ) );
        }
    }

    @SuppressWarnings( { "deprecation", "rawtypes", "unchecked" } )
    public void testWatchedBeans()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();
        List<String> names = new ArrayList<String>();

        locator.watch( Key.get( Bean.class, Named.class ), new RankingMediator(), names );

        assertTrue( names.isEmpty() );

        locator.add( parent, 0 );

        checkNames( names, "A", "B", "C" );

        locator.add( child1, 1 );

        checkNames( names, "A", "B", "C", "X" );

        locator.remove( parent );

        checkNames( names, "X" );

        locator.add( child1, 42 );

        checkNames( names, "X" );

        locator.add( child3, 3 );

        checkNames( names, "X", "Z" );

        locator.add( parent, 2 );

        checkNames( names, "X", "Z", "A", "B", "C" );

        locator.clear();

        checkNames( names );

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

    @SuppressWarnings( { "deprecation", "rawtypes", "unchecked" } )
    public void testBrokenWatcher()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Object keepAlive = new Object();

        locator.add( parent, 0 );
        locator.watch( Key.get( Bean.class, Named.class ), new BrokenMediator(), keepAlive );
        locator.remove( parent );
    }

    private static void checkNames( final Iterable<String> actual, final String... expected )
    {
        final Iterator<String> itr = actual.iterator();
        for ( final String n : expected )
        {
            assertEquals( n, itr.next() );
        }
        assertFalse( itr.hasNext() );
    }
}
