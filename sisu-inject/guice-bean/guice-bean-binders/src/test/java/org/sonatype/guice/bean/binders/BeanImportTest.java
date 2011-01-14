/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.binders;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.sonatype.inject.Nullable;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

public class BeanImportTest
    extends TestCase
{
    @Target( FIELD )
    @Retention( RUNTIME )
    @BindingAnnotation
    public @interface Fuzzy
    {
    }

    static class FuzzyImpl
        implements Fuzzy
    {
        public Class<? extends Annotation> annotationType()
        {
            return Fuzzy.class;
        }

        @Override
        public boolean equals( final Object rhs )
        {
            return rhs instanceof Fuzzy;
        }

        @Override
        public int hashCode()
        {
            return 0;
        }
    }

    interface X
    {
    }

    interface Y
    {
    }

    interface Z<T>
    {
    }

    @ProvidedBy( XProvider.class )
    interface ImplicitX
        extends X
    {
    }

    @ImplementedBy( YImpl.class )
    interface ImplicitY
        extends Y
    {
    }

    static class XProvider
        implements Provider<ImplicitX>
    {
        public ImplicitX get()
        {
            return new ImplicitX()
            {
            };
        }
    }

    static abstract class AbstractY
        implements Y
    {
    }

    static class YImpl
        extends AbstractY
        implements ImplicitY
    {
    }

    static class ZImpl<T>
        implements Z<T>
    {
        T element;
    }

    static abstract class AbstractX
        implements X
    {
        @Inject
        Injector injector;

        @Inject
        Logger logger;

        @Inject
        ImplicitX implicitX;

        @Inject
        ImplicitY implicitY;

        @Inject
        YImpl concreteY;

        @Inject
        @Nullable
        AbstractY abstractY;

        @Inject
        @Fuzzy
        Y fuzzy;

        @Inject
        @Named( "local" )
        Y local;

        @Inject
        Map<Named, Y> namedMap;
    }

    static class UnrestrictedInstance
        extends AbstractX
    {
        final Y single;

        @Inject
        UnrestrictedInstance( @Nullable final Y single, @Named( "local" ) final Y local )
        {
            this.single = single;
            this.local = local;
        }
    }

    static class UnrestrictedList
        extends AbstractX
    {
        @Inject
        List<Y> list;
    }

    static class NamedType
        extends AbstractX
    {
        final Y single;

        @Inject
        NamedType( @Named( "local" ) final Y local, @Nullable @Named final Y single )
        {
            this.single = single;
            this.local = local;
        }
    }

    static class NamedInstance
        extends AbstractX
    {
        final Y single;

        @Inject
        NamedInstance( @Nullable @Named( "TEST" ) final Y single )
        {
            this.single = single;
        }

        @Inject
        void setLocal( final @Named( "local" ) Y local )
        {
            this.local = local;
        }
    }

    static class HintMap
        extends AbstractX
    {
        @Inject
        Map<String, Y> map;
    }

    static class PlaceholderInstance
        extends AbstractX
    {
        @Inject
        @Nullable
        @Named( "${name}" )
        Y single;
    }

    static class PlaceholderString
        extends AbstractX
    {
        @Inject
        @Nullable
        @Named( "${text}" )
        String single;
    }

    static class PlaceholderConfig
        extends AbstractX
    {
        @Inject
        @Nullable
        @Named( "4${value}2" )
        int single;
    }

    static class BadMap
        implements X
    {
        @Inject
        Map<Integer, Integer> map;
    }

    static class RawList
        implements X
    {
        @Inject
        @SuppressWarnings( "rawtypes" )
        List list;
    }

    static class RawMap
        implements X
    {
        @Inject
        @SuppressWarnings( "rawtypes" )
        Map map;
    }

    static class MissingList
        implements X
    {
        @Inject
        @Named( "missing" )
        List<Y> list;
    }

    static class MissingMap
        implements X
    {
        @Inject
        @Named( "missing" )
        Map<Named, Y> map;
    }

    static class GenericInstance
        implements X
    {
        @Inject
        Z<Integer> integer;

        @Inject
        Z<String> string;
    }

    static Map<String, String> PROPS = new HashMap<String, String>();

    class TestModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bindInterceptor( Matchers.subclassesOf( X.class ), Matchers.any() );
            requestInjection( BeanImportTest.this );

            bind( X.class ).annotatedWith( Names.named( "UI" ) ).to( UnrestrictedInstance.class );
            bind( X.class ).annotatedWith( Names.named( "UL" ) ).to( UnrestrictedList.class );

            bind( X.class ).annotatedWith( Names.named( "NT" ) ).to( NamedType.class );
            bind( X.class ).annotatedWith( Names.named( "NI" ) ).to( NamedInstance.class );
            bind( X.class ).annotatedWith( Names.named( "HM" ) ).to( HintMap.class );

            bind( X.class ).annotatedWith( Names.named( "PI" ) ).to( PlaceholderInstance.class );
            bind( X.class ).annotatedWith( Names.named( "PS" ) ).to( PlaceholderString.class );
            bind( X.class ).annotatedWith( Names.named( "PC" ) ).to( PlaceholderConfig.class );

            bind( X.class ).annotatedWith( Names.named( "GI" ) ).to( GenericInstance.class );

            bind( Y.class ).annotatedWith( Names.named( "local" ) ).toInstance( new YImpl() );
            bind( Y.class ).annotatedWith( new FuzzyImpl() ).toInstance( new YImpl() );

            bind( Z.class ).to( ZImpl.class );

            bind( ParameterKeys.PROPERTIES ).toInstance( PROPS );
        }
    }

    public void testUnrestrictedImport()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final UnrestrictedInstance unrestrictedInstance =
            (UnrestrictedInstance) injector.getInstance( Key.get( X.class, Names.named( "UI" ) ) );

        assertSame( unrestrictedInstance.local, unrestrictedInstance.single );

        final UnrestrictedList unrestrictedList =
            (UnrestrictedList) injector.getInstance( Key.get( X.class, Names.named( "UL" ) ) );

        assertEquals( 2, unrestrictedList.list.size() );

        assertSame( unrestrictedInstance.local, unrestrictedList.list.get( 0 ) );
        assertSame( unrestrictedList.local, unrestrictedList.list.get( 0 ) );

        assertSame( unrestrictedInstance.fuzzy, unrestrictedList.list.get( 1 ) );
        assertSame( unrestrictedList.fuzzy, unrestrictedList.list.get( 1 ) );

        assertNotSame( unrestrictedList.list.get( 0 ), unrestrictedList.list.get( 1 ) );
    }

    public void testNamedImports()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final NamedType namedType = (NamedType) injector.getInstance( Key.get( X.class, Names.named( "NT" ) ) );
        final NamedInstance namedInstance =
            (NamedInstance) injector.getInstance( Key.get( X.class, Names.named( "NI" ) ) );

        assertNotNull( namedType.single );
        assertSame( namedType.local, namedType.single );
        assertNull( namedInstance.single );

        final HintMap hintMap = (HintMap) injector.getInstance( Key.get( X.class, Names.named( "HM" ) ) );
        assertEquals( Collections.singletonMap( Names.named( "local" ), hintMap.local ), hintMap.namedMap );
        assertSame( namedType.local, hintMap.map.get( "local" ) );
        assertSame( hintMap.local, hintMap.map.get( "local" ) );
        assertEquals( 1, hintMap.map.size() );
    }

    public void testPlaceholderImports()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        PlaceholderInstance placeholderInstance;
        placeholderInstance = (PlaceholderInstance) injector.getInstance( Key.get( X.class, Names.named( "PI" ) ) );
        assertNull( placeholderInstance.single );

        PROPS.put( "name", "local" );

        placeholderInstance = (PlaceholderInstance) injector.getInstance( Key.get( X.class, Names.named( "PI" ) ) );
        assertSame( placeholderInstance.local, placeholderInstance.single );

        PlaceholderString placeholderString;
        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( "${text}", placeholderString.single );

        PROPS.put( "text", "Hello, world!" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( "Hello, world!", placeholderString.single );

        PROPS.put( "text", ">${one}{" );
        PROPS.put( "one", "-${two}=" );
        PROPS.put( "two", "<${three}}" );
        PROPS.put( "three", "|${text}|" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( ">-<|>-<|${text}|}={|}={", placeholderString.single );

        PROPS.put( "text", ">${text" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( ">${text", placeholderString.single );

        try
        {
            injector.getInstance( Key.get( X.class, Names.named( "PC" ) ) );
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
            System.out.println( e );
        }

        PROPS.put( "value", "53" );

        assertEquals( 4532,
                      ( (PlaceholderConfig) injector.getInstance( Key.get( X.class, Names.named( "PC" ) ) ) ).single );
    }

    public void testDuplicatesAreIgnored()
    {
        Guice.createInjector( new WireModule( new TestModule(), new TestModule(), new TestModule() ) );
    }

    public void testImportSource()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );
        assertEquals( ImportBinder.class.getName(), injector.getBinding( Y.class ).getSource().toString() );
    }

    public void testInvalidTypeParameters()
    {
        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "BM" ) ).to( BadMap.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "RL" ) ).to( RawList.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "RM" ) ).to( RawMap.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "ML" ) ).to( MissingList.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "MM" ) ).to( MissingMap.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }
    }

    public void testGenericInjection()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final GenericInstance genericInstance =
            (GenericInstance) injector.getInstance( Key.get( X.class, Names.named( "GI" ) ) );

        assertNotNull( genericInstance.integer );
        assertNotNull( genericInstance.string );
    }
}
