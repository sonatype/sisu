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
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Jsr330;

public class BeanEntriesByRoleHintTest
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

        final Iterable<Entry<String, Bean>> roles =
            new BeanEntriesByRoleHint<Bean>( injector, TypeLiteral.get( Bean.class ), "A", Hints.DEFAULT_HINT, "B", "C" );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean aBean, bBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        aBean = mapping.getValue();
        assertEquals( "A", mapping.getKey() );
        assertEquals( ABean.class, aBean.getClass() );
        assertSame( aBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        assertSame( Hints.DEFAULT_HINT, mapping.getKey() );

        try
        {
            mapping.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            System.out.println( e );
        }

        try
        {
            mapping.setValue( null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertSame( Hints.DEFAULT_HINT, i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );

        i = roles.iterator();
        assertSame( aBean, i.next().getValue() );
        i.next();
        assertSame( bBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );

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

    public void testRoleHintsWithImplicitDefault()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
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
        } );

        final Iterable<Entry<String, ImplicitDefaultBean>> roles =
            new BeanEntriesByRoleHint<ImplicitDefaultBean>( injector, TypeLiteral.get( ImplicitDefaultBean.class ),
                                                            "A", Hints.DEFAULT_HINT, "B", "C" );

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

        defaultBean = mapping.getValue();
        assertSame( Hints.DEFAULT_HINT, mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertSame( Hints.DEFAULT_HINT, i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );

        i = roles.iterator();
        assertSame( aBean, i.next().getValue() );
        assertSame( defaultBean, i.next().getValue() );
        assertSame( bBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );

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
                bind( Bean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to( DefaultBean.class );
                bind( Bean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
                bind( Bean.class ).to( DefaultBean.class );
            }
        } );

        final Iterable<Entry<String, Bean>> roles =
            new BeanEntriesByRoleHint<Bean>( injector, TypeLiteral.get( Bean.class ), "A", Hints.DEFAULT_HINT, "B", "C" );

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

        defaultBean = mapping.getValue();
        assertSame( Hints.DEFAULT_HINT, mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertSame( Hints.DEFAULT_HINT, i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );

        i = roles.iterator();
        assertSame( aBean, i.next().getValue() );
        assertSame( defaultBean, i.next().getValue() );
        assertSame( bBean, i.next().getValue() );
        assertSame( cBean, i.next().getValue() );

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

    public void testChildRoleHintsWithImplicitDefault()
    {
        final Injector parentInjector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "C" ) ).to( CBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( Hints.DEFAULT_HINT ) ).to(
                                                                                                          DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "" ) ).to( DefaultBean.class );
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "A" ) ).to( ABean.class );
            }
        } );

        final Injector injector = parentInjector.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ImplicitDefaultBean.class ).annotatedWith( Jsr330.named( "B" ) ).to( BBean.class );
            }
        } );

        final Iterable<Entry<String, ImplicitDefaultBean>> roles =
            new BeanEntriesByRoleHint<ImplicitDefaultBean>( injector, TypeLiteral.get( ImplicitDefaultBean.class ),
                                                            "A", Hints.DEFAULT_HINT, "B", "C" );

        Iterator<Entry<String, ImplicitDefaultBean>> i;
        Entry<String, ImplicitDefaultBean> mapping;
        ImplicitDefaultBean bBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        assertSame( "A", mapping.getKey() );

        try
        {
            mapping.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            System.out.println( e );
        }

        assertTrue( i.hasNext() );
        mapping = i.next();

        assertSame( Hints.DEFAULT_HINT, mapping.getKey() );

        try
        {
            mapping.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            System.out.println( e );
        }

        assertTrue( i.hasNext() );
        mapping = i.next();

        bBean = mapping.getValue();
        assertEquals( "B", mapping.getKey() );
        assertEquals( BBean.class, bBean.getClass() );
        assertSame( bBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        assertSame( "C", mapping.getKey() );

        try
        {
            mapping.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            System.out.println( e );
        }

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertSame( Hints.DEFAULT_HINT, i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );

        i = roles.iterator();
        i.next();
        i.next();
        assertSame( bBean, i.next().getValue() );
        i.next();

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

        final Iterable<Entry<String, Bean>> roles =
            new BeanEntriesByRoleHint<Bean>( injector, TypeLiteral.get( Bean.class ), "A", Hints.DEFAULT_HINT, "B", "C" );

        Iterator<Entry<String, Bean>> i;
        Entry<String, Bean> mapping;
        Bean defaultBean, cBean;

        i = roles.iterator();
        assertTrue( i.hasNext() );
        mapping = i.next();

        assertSame( "A", mapping.getKey() );

        try
        {
            mapping.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            System.out.println( e );
        }

        assertTrue( i.hasNext() );
        mapping = i.next();

        defaultBean = mapping.getValue();
        assertSame( Hints.DEFAULT_HINT, mapping.getKey() );
        assertEquals( DefaultBean.class, defaultBean.getClass() );
        assertSame( defaultBean, mapping.getValue() );

        assertTrue( i.hasNext() );
        mapping = i.next();

        assertSame( "B", mapping.getKey() );

        try
        {
            mapping.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            System.out.println( e );
        }

        assertTrue( i.hasNext() );
        mapping = i.next();

        cBean = mapping.getValue();
        assertEquals( "C", mapping.getKey() );
        assertEquals( CBean.class, cBean.getClass() );
        assertSame( cBean, mapping.getValue() );

        assertFalse( i.hasNext() );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertSame( Hints.DEFAULT_HINT, i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );

        i = roles.iterator();
        i.next();
        assertSame( defaultBean, i.next().getValue() );
        i.next();
        assertSame( cBean, i.next().getValue() );

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