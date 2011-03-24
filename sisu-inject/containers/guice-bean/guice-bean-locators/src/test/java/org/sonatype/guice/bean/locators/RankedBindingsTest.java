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
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class RankedBindingsTest
    extends TestCase
{
    @ImplementedBy( BeanImpl2.class )
    static interface Bean
    {
    }

    static abstract class AbstractBean
        implements Bean
    {
    }

    static class BeanImpl
        extends AbstractBean
    {
    }

    static class BeanImpl2
        extends AbstractBean
    {
    }

    static Injector injector0 = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( ClassSpace.class ).toInstance( new URLClassSpace( Bean.class.getClassLoader() ) );
        }
    } );

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

    public void testImplicitBindings()
    {
        RankedBindings<?> bindings;

        bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), null );
        bindings.add( new InjectorPublisher( injector0, new DefaultRankingFunction( 0 ) ), 0 );
        final RankedBindings<?>.Itr itr = bindings.iterator();

        // injector 'owns' Bean
        assertTrue( itr.hasNext() );
        assertEquals( BeanImpl2.class, itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) );

        bindings = new RankedBindings<String>( TypeLiteral.get( String.class ), null );
        bindings.add( new InjectorPublisher( injector0, new DefaultRankingFunction( 0 ) ), 0 );

        // injector doesn't 'own' String
        assertFalse( bindings.iterator().hasNext() );

        bindings = new RankedBindings<AbstractBean>( TypeLiteral.get( AbstractBean.class ), null );
        bindings.add( new InjectorPublisher( injector0, new DefaultRankingFunction( 0 ) ), 0 );

        // AbstractBean has no implicit binding
        assertFalse( bindings.iterator().hasNext() );
    }

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

        function = new DefaultRankingFunction( 0 );
        exporters.insert( new InjectorPublisher( injector0, function ), function.maxRank() );
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

        final Binding<Bean> explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertEquals( 3, bindings.bindings.size() );
        assertTrue( itr.hasNext() );
        assertEquals( 5, bindings.bindings.size() );

        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );

        final Binding<Bean> implicitBinding = itr.next();
        assertNull( implicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl2.class, implicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

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
        Binding<Bean> explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 2, bindings.bindings.size() );
        function = new DefaultRankingFunction( 0 );
        bindings.add( new InjectorPublisher( injector0, function ), function.maxRank() );
        function = new DefaultRankingFunction( 3 );
        bindings.add( new InjectorPublisher( injector3, function ), function.maxRank() );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 4, bindings.bindings.size() );

        assertEquals( 4, bindings.bindings.size() );
        function = new DefaultRankingFunction( 1 );
        bindings.add( new InjectorPublisher( injector1, function ), function.maxRank() );
        assertEquals( 4, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 5, bindings.bindings.size() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertEquals( 5, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 5, bindings.bindings.size() );

        itr = bindings.iterator();

        explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );

        final Binding<Bean> implicitBinding = itr.next();
        assertNull( implicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl2.class, implicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

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
        final BindingPublisher exporter0 = new InjectorPublisher( injector0, new DefaultRankingFunction( 0 ) );
        final BindingPublisher exporter1 = new InjectorPublisher( injector1, new DefaultRankingFunction( 1 ) );
        final BindingPublisher exporter2 = new InjectorPublisher( injector2, new DefaultRankingFunction( 2 ) );
        final BindingPublisher exporter3 = new InjectorPublisher( injector3, new DefaultRankingFunction( 3 ) );

        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), null );

        bindings.add( exporter0, 0 );
        bindings.add( exporter1, 1 );
        bindings.add( exporter2, 2 );
        bindings.add( exporter3, 3 );

        Iterator<Binding<Bean>> itr = bindings.iterator();

        bindings.remove( exporter1 );
        assertTrue( itr.hasNext() );

        Binding<Bean> explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        bindings.remove( injector3.findBindingsByType( TypeLiteral.get( Bean.class ) ).get( 0 ) );
        bindings.remove( exporter2 );
        bindings.remove( injector1.findBindingsByType( TypeLiteral.get( Bean.class ) ).get( 0 ) );

        assertTrue( itr.hasNext() );

        Binding<Bean> implicitBinding = itr.next();
        assertNull( implicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl2.class, implicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertFalse( itr.hasNext() );

        itr = bindings.iterator();

        bindings.clear();

        bindings.add( exporter3, 0 );
        bindings.add( exporter1, 0 );
        bindings.add( exporter0, 0 );
        bindings.add( exporter2, 0 );

        assertTrue( itr.hasNext() );

        explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );

        implicitBinding = itr.next();
        assertNull( implicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl2.class, implicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertFalse( itr.hasNext() );
        assertFalse( itr.hasNext() );
    }
}
