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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.spi.BindingPublisher;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class RankedBindingsTest
    extends TestCase
{
    static interface Bean
    {
    }

    static class BeanImpl
        implements Bean
    {
    }

    static class BeanImpl2
        implements Bean
    {
    }

    static Injector injector1 = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Bean.class ).annotatedWith( Names.named( "1" ) ).to( BeanImpl.class );
        }
    } );

    static Injector injector2 = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Bean.class ).annotatedWith( Names.named( "2" ) ).to( BeanImpl.class );
            bind( Bean.class ).to( BeanImpl.class );
        }
    } );

    static Injector injector3 = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Bean.class ).annotatedWith( Names.named( "3" ) ).to( BeanImpl.class );
        }
    } );

    public void testExistingExporters()
    {
        final RankedList<BindingPublisher> exporters = new RankedList<BindingPublisher>();

        RankingFunction function;

        try
        {
            new DefaultRankingFunction( -1 );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
            // expected
        }

        function = new DefaultRankingFunction( 1 );
        exporters.insert( new InjectorPublisher( injector1, function ), function.maxRank() );
        function = new DefaultRankingFunction( 3 );
        exporters.insert( new InjectorPublisher( injector3, function ), function.maxRank() );
        function = new DefaultRankingFunction( 2 );
        exporters.insert( new InjectorPublisher( injector2, function ), function.maxRank() );

        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), exporters );

        final Iterator<Binding<Bean>> itr = bindings.iterator();

        assertEquals( 0, bindings.bindings.size() );
        assertTrue( itr.hasNext() );
        assertEquals( 3, bindings.bindings.size() );

        assertNull( itr.next().getKey().getAnnotation() );

        assertEquals( 3, bindings.bindings.size() );
        assertTrue( itr.hasNext() );
        assertEquals( 4, bindings.bindings.size() );

        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );

        assertFalse( itr.hasNext() );
    }

    public void testPendingExporters()
    {
        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), null );

        Iterator<Binding<Bean>> itr = bindings.iterator();

        assertFalse( itr.hasNext() );

        try
        {
            itr.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
            // expected
        }

        try
        {
            itr.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        RankingFunction function;

        assertEquals( 0, bindings.bindings.size() );
        function = new DefaultRankingFunction( 2 );
        bindings.add( new InjectorPublisher( injector2, function ), function.maxRank() );
        assertEquals( 0, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 2, bindings.bindings.size() );
        assertNull( itr.next().getKey().getAnnotation() );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 2, bindings.bindings.size() );
        function = new DefaultRankingFunction( 3 );
        bindings.add( new InjectorPublisher( injector3, function ), function.maxRank() );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 3, bindings.bindings.size() );

        assertEquals( 3, bindings.bindings.size() );
        function = new DefaultRankingFunction( 1 );
        bindings.add( new InjectorPublisher( injector1, function ), function.maxRank() );
        assertEquals( 3, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 4, bindings.bindings.size() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertEquals( 4, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 4, bindings.bindings.size() );

        itr = bindings.iterator();

        assertNull( itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );

        assertFalse( itr.hasNext() );
    }

    public void testIsActive()
    {
        final Key<Bean> key = Key.get( TypeLiteral.get( Bean.class ) );
        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( key.getTypeLiteral(), null );

        assertFalse( bindings.isActive() );
        LocatedBeans<Named, Bean> namedBeans1 = new LocatedBeans<Named, Bean>( key, bindings );
        assertTrue( bindings.isActive() );
        LocatedBeans<Named, Bean> namedBeans2 = new LocatedBeans<Named, Bean>( key, bindings );
        assertTrue( bindings.isActive() );
        LocatedBeans<Named, Bean> namedBeans3 = new LocatedBeans<Named, Bean>( key, bindings );
        assertTrue( bindings.isActive() );

        assertFalse( namedBeans1.iterator().hasNext() );
        assertFalse( namedBeans2.iterator().hasNext() );
        assertFalse( namedBeans3.iterator().hasNext() );

        bindings.add( new InjectorPublisher( injector1, new DefaultRankingFunction( 1 ) ), 1 );

        assertTrue( namedBeans1.iterator().hasNext() );
        assertTrue( namedBeans2.iterator().hasNext() );
        assertTrue( namedBeans3.iterator().hasNext() );

        namedBeans2 = null;
        System.gc();

        assertTrue( namedBeans1.iterator().hasNext() );
        assertTrue( namedBeans3.iterator().hasNext() );

        bindings.remove( new InjectorPublisher( injector1, null ) );

        assertFalse( namedBeans1.iterator().hasNext() );
        assertFalse( namedBeans3.iterator().hasNext() );

        assertTrue( bindings.isActive() );

        namedBeans1 = null;
        namedBeans3 = null;
        System.gc();

        assertFalse( bindings.isActive() );
    }

    public void testExporterRemoval()
    {
        final BindingPublisher exporter1 = new InjectorPublisher( injector1, new DefaultRankingFunction( 1 ) );
        final BindingPublisher exporter2 = new InjectorPublisher( injector2, new DefaultRankingFunction( 2 ) );
        final BindingPublisher exporter3 = new InjectorPublisher( injector3, new DefaultRankingFunction( 3 ) );

        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), null );

        bindings.add( exporter1, 1 );
        bindings.add( exporter2, 2 );
        bindings.add( exporter3, 3 );

        Iterator<Binding<Bean>> itr = bindings.iterator();

        bindings.remove( exporter1 );
        assertTrue( itr.hasNext() );
        assertNull( itr.next().getKey().getAnnotation() );
        bindings.remove( injector3.findBindingsByType( TypeLiteral.get( Bean.class ) ).get( 0 ) );
        bindings.remove( exporter2 );
        bindings.remove( injector1.findBindingsByType( TypeLiteral.get( Bean.class ) ).get( 0 ) );
        assertFalse( itr.hasNext() );

        itr = bindings.iterator();

        bindings.clear();

        bindings.add( exporter3, 0 );
        bindings.add( exporter1, 0 );
        bindings.add( exporter2, 0 );

        assertTrue( itr.hasNext() );
        assertNull( itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );
        assertFalse( itr.hasNext() );

        assertFalse( itr.hasNext() );
    }
}
