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
package org.sonatype.guice.bean.binders;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import junit.framework.TestCase;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;
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
    @Target( TYPE )
    @Retention( RUNTIME )
    @Qualifier
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

    @Retention( RetentionPolicy.RUNTIME )
    public @interface Nullable
    {
    }

    interface X
    {
    }

    interface Y
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

    static class YImpl
        implements ImplicitY
    {
    }

    abstract static class Abstract
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
        @Named( "local" )
        Y local;

        @Inject
        Map<Named, Y> namedMap;
    }

    static class UnrestrictedInstance
        extends Abstract
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
        extends Abstract
    {
        @Inject
        List<Y> list;
    }

    static class UnrestrictedMap
        extends Abstract
    {
        @Inject
        Map<Annotation, Y> map;
    }

    static class NamedType
        extends Abstract
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
        extends Abstract
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
        extends Abstract
    {
        @Inject
        Map<String, Y> map;
    }

    static class PlaceholderInstance
        extends Abstract
    {
        @Inject
        @Nullable
        @Named( "${name}" )
        Y single;
    }

    static class PlaceholderString
        extends Abstract
    {
        @Inject
        @Nullable
        @Named( "${text}" )
        String single;
    }

    static class PlaceholderConfig
        extends Abstract
    {
        @Inject
        @Nullable
        @Named( "${value}" )
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
            bind( X.class ).annotatedWith( Names.named( "UM" ) ).to( UnrestrictedMap.class );

            bind( X.class ).annotatedWith( Names.named( "NT" ) ).to( NamedType.class );
            bind( X.class ).annotatedWith( Names.named( "NI" ) ).to( NamedInstance.class );
            bind( X.class ).annotatedWith( Names.named( "HM" ) ).to( HintMap.class );

            bind( X.class ).annotatedWith( Names.named( "PI" ) ).to( PlaceholderInstance.class );
            bind( X.class ).annotatedWith( Names.named( "PS" ) ).to( PlaceholderString.class );
            bind( X.class ).annotatedWith( Names.named( "PC" ) ).to( PlaceholderConfig.class );

            bind( Y.class ).annotatedWith( Names.named( "local" ) ).toInstance( new YImpl() );
            bind( Y.class ).annotatedWith( new FuzzyImpl() ).toInstance( new YImpl() );
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

        assertSame( unrestrictedInstance.local, unrestrictedList.list.get( 0 ) );
        assertSame( unrestrictedList.local, unrestrictedList.list.get( 0 ) );
        assertNotSame( unrestrictedList.list.get( 0 ), unrestrictedList.list.get( 1 ) );
        assertEquals( 2, unrestrictedList.list.size() );

        final UnrestrictedMap unrestrictedMap =
            (UnrestrictedMap) injector.getInstance( Key.get( X.class, Names.named( "UM" ) ) );

        assertSame( unrestrictedList.list.get( 0 ), unrestrictedMap.map.get( Names.named( "local" ) ) );
        assertSame( unrestrictedList.list.get( 1 ), unrestrictedMap.map.get( new FuzzyImpl() ) );
        assertEquals( 2, unrestrictedMap.map.size() );
    }

    public void testNamedImports()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final NamedType namedType = (NamedType) injector.getInstance( Key.get( X.class, Names.named( "NT" ) ) );
        final NamedInstance namedInstance =
            (NamedInstance) injector.getInstance( Key.get( X.class, Names.named( "NI" ) ) );

        assertNotNull( namedType.single );
        assertNull( namedInstance.single );

        assertSame( namedType.local, namedType.single );
        assertSame( namedType.local, namedInstance.local );

        final HintMap hintMap = (HintMap) injector.getInstance( Key.get( X.class, Names.named( "HM" ) ) );
        assertEquals( Collections.singletonMap( Names.named( "local" ), hintMap.local ), hintMap.namedMap );
        assertSame( namedType.local, hintMap.map.get( "local" ) );
        assertSame( hintMap.local, hintMap.map.get( "local" ) );
        assertEquals( 1, hintMap.map.size() );
    }

    public void testDuplicatesAreIgnored()
    {
        Guice.createInjector( new WireModule( new TestModule(), new TestModule(), new TestModule() ) );
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
}
