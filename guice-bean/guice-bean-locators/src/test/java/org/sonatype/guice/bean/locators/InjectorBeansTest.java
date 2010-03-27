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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import javax.inject.Named;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Jsr330;

public class InjectorBeansTest
    extends TestCase
{
    interface Bean
    {
    }

    @ImplementedBy( DefaultBean.class )
    interface ImplicitDefaultBean
        extends Bean
    {
    }

    static class DefaultBean
        implements ImplicitDefaultBean
    {
    }

    static class ABean
        implements ImplicitDefaultBean
    {
    }

    static class BBean
        implements ImplicitDefaultBean
    {
    }

    static class CBean
        implements ImplicitDefaultBean
    {
    }

    // ****************************************************************************
    // ** @Named("default") bindings are _not_ considered to be default bindings **
    // ****************************************************************************

    public void testRolesWithNoDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final Iterable<Entry<Named, Bean>> namedBeans =
            new InjectorBeans<Named, Bean>( injector, Key.get( Bean.class, Named.class ) );

        Iterator<Entry<Named, Bean>> i;
        Entry<Named, Bean> mapping;
        Bean aBean, bBean, cBean;

        i = namedBeans.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        try
        {
            mapping.setValue( null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey().value() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey().value() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey().value() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = namedBeans.iterator();
        assertEquals( "C", i.next().getKey().value() );
        assertEquals( "A", i.next().getKey().value() );
        assertEquals( "B", i.next().getKey().value() );

        i = namedBeans.iterator();
        assertSame( cBean, i.next().getValue() );
        assertSame( aBean, i.next().getValue() );
        assertSame( bBean, i.next().getValue() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testRolesWithImplicitDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final Iterable<Entry<Named, ImplicitDefaultBean>> namedBeans =
            new InjectorBeans<Named, ImplicitDefaultBean>( injector, Key.get( ImplicitDefaultBean.class, Named.class ) );

        Iterator<Entry<Named, ImplicitDefaultBean>> i;
        Entry<Named, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean aBean, bBean, cBean;

        i = namedBeans.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey().value() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey().value() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey().value() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = namedBeans.iterator();
        assertEquals( "C", i.next().getKey().value() );
        assertEquals( "A", i.next().getKey().value() );
        assertEquals( "B", i.next().getKey().value() );

        i = namedBeans.iterator();
        assertSame( cBean, i.next().getValue() );
        assertSame( aBean, i.next().getValue() );
        assertSame( bBean, i.next().getValue() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testRolesWithExplicitDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
                bind( Bean.class ).to( DefaultBean.class );
            }
        } );

        final Iterable<Entry<Named, Bean>> namedBeans =
            new InjectorBeans<Named, Bean>( injector, Key.get( Bean.class, Named.class ) );

        Iterator<Entry<Named, Bean>> i;
        Entry<Named, Bean> mapping;
        Bean defaultBean, aBean, bBean, cBean;

        i = namedBeans.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertNull( mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey().value() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey().value() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey().value() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = namedBeans.iterator();
        assertNull( i.next().getKey() );
        assertEquals( "C", i.next().getKey().value() );
        assertEquals( "A", i.next().getKey().value() );
        assertEquals( "B", i.next().getKey().value() );

        i = namedBeans.iterator();
        assertSame( defaultBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );
        assertSame( aBean, i.next().getValue() );
        assertSame( bBean, i.next().getValue() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testChildRolesWithImplicitDefault()
    {
        final Injector parentInjector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final Injector injector = parentInjector.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
            }
        } );

        final Iterable<Entry<Named, ImplicitDefaultBean>> namedBeans =
            new InjectorBeans<Named, ImplicitDefaultBean>( injector, Key.get( ImplicitDefaultBean.class, Named.class ) );

        Iterator<Entry<Named, ImplicitDefaultBean>> i;
        Entry<Named, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean aBean;

        i = namedBeans.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey().value() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = namedBeans.iterator();
        assertEquals( "A", i.next().getKey().value() );

        i = namedBeans.iterator();
        assertSame( aBean, i.next().getValue() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testChildRolesWithExplicitDefault()
    {
        final Injector parentInjector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final Injector injector = parentInjector.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).to( DefaultBean.class );
            }
        } );

        final Iterable<Entry<Named, Bean>> namedBeans =
            new InjectorBeans<Named, Bean>( injector, Key.get( Bean.class, Named.class ) );

        Iterator<Entry<Named, Bean>> i;
        Entry<Named, Bean> mapping;
        Bean defaultBean, cBean;

        i = namedBeans.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertNull( mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey().value() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = namedBeans.iterator();
        assertNull( i.next().getKey() );
        assertEquals( "C", i.next().getKey().value() );

        i = namedBeans.iterator();
        assertSame( defaultBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testQualifierInstance()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
            }
        } );

        final Iterable<Entry<Named, Bean>> namedBeans =
            new InjectorBeans<Named, Bean>( injector, Key.get( Bean.class, Jsr330.named( "B" ) ) );

        Iterator<Entry<Named, Bean>> i;
        Entry<Named, Bean> mapping;
        Bean bBean;

        i = namedBeans.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey().value() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = namedBeans.iterator();
        assertEquals( "B", i.next().getKey().value() );

        i = namedBeans.iterator();
        assertSame( bBean, i.next().getValue() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testUnrestrictedSearch()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
                bind( Bean.class ).to( DefaultBean.class );
            }
        } );

        final Iterable<Entry<Annotation, Bean>> beans =
            new InjectorBeans<Annotation, Bean>( injector, Key.get( Bean.class ) );

        Iterator<Entry<Annotation, Bean>> i;
        Entry<Annotation, Bean> mapping;
        Bean defaultBean, aBean, bBean, cBean, pBean;

        i = beans.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertNull( mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( Jsr330.named( "C" ), mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( Jsr330.named( "A" ), mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        pBean = mapping.getValue();
        assertEquals( Names.named( "!" ), mapping.getKey() );
        assertEquals( CBean.class, pBean.getClass() );
        assertSame( pBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( Jsr330.named( "B" ), mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = beans.iterator();
        assertNull( i.next().getKey() );
        assertEquals( Jsr330.named( "C" ), i.next().getKey() );
        assertEquals( Jsr330.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "!" ), i.next().getKey() );
        assertEquals( Jsr330.named( "B" ), i.next().getKey() );

        i = beans.iterator();
        assertSame( defaultBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );
        assertSame( aBean, i.next().getValue() );
        assertSame( pBean, i.next().getValue() );
        assertSame( bBean, i.next().getValue() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }
}
