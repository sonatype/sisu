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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.sonatype.guice.plexus.config.Hints;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Jsr330;

public class BeanEntriesByRoleTest
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

    public void testRolesWithNoDefault()
    {
        final List<Injector> injectors = new ArrayList<Injector>();
        injectors.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } ) );

        final Iterable<Entry<String, Bean>> roles =
            new BeanEntriesByRole<Bean>( injectors, TypeLiteral.get( Bean.class ) );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean aBean, bBean, cBean;

        i = roles.iterator();
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
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "C", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );

        i = roles.iterator();
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

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testRolesWithImplicitDefault()
    {
        final List<Injector> injectors = new ArrayList<Injector>();
        injectors.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to(
                                                                                                          DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } ) );

        final Iterable<Entry<String, ImplicitDefaultBean>> roles =
            new BeanEntriesByRole<ImplicitDefaultBean>( injectors, TypeLiteral.get( ImplicitDefaultBean.class ) );

        Iterator<Entry<String, ImplicitDefaultBean>> i;
        Entry<String, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean defaultBean, aBean, bBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertSame( Hints.DEFAULT_HINT, mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertSame( Hints.DEFAULT_HINT, i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );

        i = roles.iterator();
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

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testRolesWithExplicitDefault()
    {
        final List<Injector> injectors = new ArrayList<Injector>();
        injectors.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
                bind( Bean.class ).to( DefaultBean.class );
            }
        } ) );

        final Iterable<Entry<String, Bean>> roles =
            new BeanEntriesByRole<Bean>( injectors, TypeLiteral.get( Bean.class ) );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean defaultBean, aBean, bBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertSame( Hints.DEFAULT_HINT, mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertSame( Hints.DEFAULT_HINT, i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );

        i = roles.iterator();
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

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }
}