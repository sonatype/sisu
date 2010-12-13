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

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.spi.BindingExporter;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
        final RankedList<BindingExporter> exporters = new RankedList<BindingExporter>();

        exporters.insert( new InjectorBindingExporter( injector1, (short) 1 ), 1 );
        exporters.insert( new InjectorBindingExporter( injector3, (short) 3 ), 3 );
        exporters.insert( new InjectorBindingExporter( injector2, (short) 2 ), 2 );

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

        assertEquals( 0, bindings.bindings.size() );
        bindings.add( new InjectorBindingExporter( injector2, (short) 2 ), 2 );
        assertEquals( 0, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 2, bindings.bindings.size() );
        assertNull( itr.next().getKey().getAnnotation() );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 2, bindings.bindings.size() );
        bindings.add( new InjectorBindingExporter( injector3, (short) 3 ), 3 );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );
        assertEquals( 3, bindings.bindings.size() );

        assertEquals( 3, bindings.bindings.size() );
        bindings.add( new InjectorBindingExporter( injector1, (short) 1 ), 1 );
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

    public void testExporterRemoval()
    {
        final BindingExporter exporter1 = new InjectorBindingExporter( injector1, (short) 1 );
        final BindingExporter exporter2 = new InjectorBindingExporter( injector2, (short) 2 );
        final BindingExporter exporter3 = new InjectorBindingExporter( injector3, (short) 3 );

        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), null );

        bindings.add( exporter1, 1 );
        bindings.add( exporter2, 2 );
        bindings.add( exporter3, 3 );

        Iterator<Binding<Bean>> itr = bindings.iterator();

        bindings.remove( exporter1 );
        assertNull( itr.next().getKey().getAnnotation() );
        bindings.remove( exporter2 );
        bindings.remove( exporter3 );

        assertFalse( itr.hasNext() );

        itr = bindings.iterator();

        bindings.clear();

        bindings.add( exporter3, 0 );
        bindings.add( exporter1, 0 );
        bindings.add( exporter2, 0 );

        assertNull( itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );

        assertFalse( itr.hasNext() );
    }
}
