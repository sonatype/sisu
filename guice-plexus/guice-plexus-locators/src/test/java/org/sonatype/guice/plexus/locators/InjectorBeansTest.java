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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.sonatype.guice.plexus.config.Hints;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
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
                bind( Bean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final Iterable<Entry<String, Bean>> roles = new InjectorBeans<Bean>( injector, TypeLiteral.get( Bean.class ) );

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
    }

    public void testRolesWithImplicitDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) )
                                                 .to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final Iterable<Entry<String, ImplicitDefaultBean>> roles =
            new InjectorBeans<ImplicitDefaultBean>( injector, TypeLiteral.get( ImplicitDefaultBean.class ) );

        Iterator<Entry<String, ImplicitDefaultBean>> i;
        Entry<String, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean defaultBean, aBean, bBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertTrue( Hints.isDefaultHint( mapping.getKey() ) );
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
        assertTrue( Hints.isDefaultHint( i.next().getKey() ) );
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
    }

    public void testRolesWithExplicitDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
                bind( Bean.class ).to( DefaultBean.class );
            }
        } );

        final Iterable<Entry<String, Bean>> roles = new InjectorBeans<Bean>( injector, TypeLiteral.get( Bean.class ) );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean defaultBean, aBean, bBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertTrue( Hints.isDefaultHint( mapping.getKey() ) );
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
        assertTrue( Hints.isDefaultHint( i.next().getKey() ) );
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
    }

    public void testChildRolesWithImplicitDefault()
    {
        final Injector parentInjector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) )
                                                 .to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
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

        final Iterable<Entry<String, ImplicitDefaultBean>> roles =
            new InjectorBeans<ImplicitDefaultBean>( injector, TypeLiteral.get( ImplicitDefaultBean.class ) );

        Iterator<Entry<String, ImplicitDefaultBean>> i;
        Entry<String, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean aBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );

        i = roles.iterator();
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
                bind( Bean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
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

        final Iterable<Entry<String, Bean>> roles = new InjectorBeans<Bean>( injector, TypeLiteral.get( Bean.class ) );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean defaultBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertTrue( Hints.isDefaultHint( mapping.getKey() ) );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertTrue( Hints.isDefaultHint( i.next().getKey() ) );
        assertEquals( "C", i.next().getKey() );

        i = roles.iterator();
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

    public void testRoleHintsWithNoDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
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
        } );

        final String[] hints = new String[] { "A", "C", null, "B" };

        final Iterable<Entry<String, Bean>> roles =
            new InjectorBeans<Bean>( injector, TypeLiteral.get( Bean.class ), hints );

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

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );

        i = roles.iterator();
        assertSame( aBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );
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

    public void testRoleHintsWithImplicitDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) )
                                                 .to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final String[] hints = new String[] { "A", "C", null, "B" };

        final Iterable<Entry<String, ImplicitDefaultBean>> roles =
            new InjectorBeans<ImplicitDefaultBean>( injector, TypeLiteral.get( ImplicitDefaultBean.class ), hints );

        Iterator<Entry<String, ImplicitDefaultBean>> i;
        Entry<String, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean defaultBean, aBean, bBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertTrue( Hints.isDefaultHint( mapping.getKey() ) );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertTrue( Hints.isDefaultHint( i.next().getKey() ) );
        assertEquals( "B", i.next().getKey() );

        i = roles.iterator();
        assertSame( aBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );
        assertSame( defaultBean, i.next().getValue() );
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

    public void testRoleHintsWithExplicitDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
                bind( Bean.class ).to( DefaultBean.class );
            }
        } );

        final String[] hints = new String[] { "A", "C", null, "B" };

        final Iterable<Entry<String, Bean>> roles =
            new InjectorBeans<Bean>( injector, TypeLiteral.get( Bean.class ), hints );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean defaultBean, aBean, bBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertTrue( Hints.isDefaultHint( mapping.getKey() ) );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertTrue( Hints.isDefaultHint( i.next().getKey() ) );
        assertEquals( "B", i.next().getKey() );

        i = roles.iterator();
        assertSame( aBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );
        assertSame( defaultBean, i.next().getValue() );
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

    public void testChildRoleHintsWithImplicitDefault()
    {
        final Injector parentInjector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) )
                                                 .to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
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

        final String[] hints = new String[] { "A", "C", null, "B" };

        final Iterable<Entry<String, ImplicitDefaultBean>> roles =
            new InjectorBeans<ImplicitDefaultBean>( injector, TypeLiteral.get( ImplicitDefaultBean.class ), hints );

        Iterator<Entry<String, ImplicitDefaultBean>> i;
        Entry<String, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean aBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );

        i = roles.iterator();
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

    public void testChildRoleHintsWithExplicitDefault()
    {
        final Injector parentInjector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
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

        final String[] hints = new String[] { "A", "C", null, "B" };

        final Iterable<Entry<String, Bean>> roles =
            new InjectorBeans<Bean>( injector, TypeLiteral.get( Bean.class ), hints );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean defaultBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertTrue( Hints.isDefaultHint( mapping.getKey() ) );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "C", i.next().getKey() );
        assertTrue( Hints.isDefaultHint( i.next().getKey() ) );

        i = roles.iterator();
        assertSame( cBean, i.next().getValue() );
        assertSame( defaultBean, i.next().getValue() );

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