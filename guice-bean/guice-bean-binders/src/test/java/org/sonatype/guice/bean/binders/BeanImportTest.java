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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.inject.BeanMediator;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

public class BeanImportTest
    extends TestCase
{
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

    static class YImpl
        implements Y
    {
    }

    abstract static class Abstract
        implements X
    {
        @Inject
        Injector injector;

        @Inject
        @Named( "local" )
        Y local;

        @Inject
        Map<Named, Y> namedMap;
    }

    static class Unrestricted
        extends Abstract
    {
        final Y single;

        @Inject
        Unrestricted( @Nullable final Y single, @Named( "local" ) final Y local )
        {
            this.single = single;
            this.local = local;
        }
    }

    static class NamedType
        extends Abstract
    {
        final Y single;

        @Inject
        NamedType( @Named( "local" ) final Y local, @Nullable @Named final Y single )
        {
            this.local = local;
            this.single = single;
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
    }

    static class NamedList
        extends Abstract
    {
        List<Y> list;

        @Inject
        void setList( final List<Y> _list )
        {
            list = _list;
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
        Map<String, Y> hintMap;
    }

    static class BadMap
        implements X
    {
        @Inject
        Map<Integer, Integer> badMap;
    }

    static class RawMap
        implements X
    {
        @Inject
        @SuppressWarnings( "unchecked" )
        Map rawMap;
    }

    static class RawList
        implements X
    {
        @Inject
        @SuppressWarnings( "unchecked" )
        List rawList;
    }

    static class TestModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bindInterceptor( Matchers.subclassesOf( X.class ), Matchers.any() );

            bind( X.class ).annotatedWith( Names.named( "U" ) ).to( Unrestricted.class ).asEagerSingleton();
            bind( X.class ).annotatedWith( Names.named( "NT" ) ).to( NamedType.class ).asEagerSingleton();
            bind( X.class ).annotatedWith( Names.named( "NI" ) ).to( NamedInstance.class ).asEagerSingleton();
            bind( X.class ).annotatedWith( Names.named( "NL" ) ).to( NamedList.class ).asEagerSingleton();
            bind( X.class ).annotatedWith( Names.named( "H" ) ).to( HintMap.class ).asEagerSingleton();

            bind( Y.class ).annotatedWith( Names.named( "local" ) ).toInstance( new YImpl() );
        }
    }

    static class TestLocator
        implements BeanLocator
    {
        static List<Key<?>> locatedKeys = new ArrayList<Key<?>>();

        public <Q extends Annotation, T> Iterable<Entry<Q, T>> locate( final Key<T> key )
        {
            locatedKeys.add( key );
            return Collections.emptyList();
        }

        public <Q extends Annotation, T, W> void watch( final Key<T> key, final BeanMediator<Q, T, W> mediator,
                                                        final W watcher )
        {
        }
    }

    static class LocatorModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind( BeanLocator.class ).to( TestLocator.class );
        }
    }

    public void testAutoImports()
    {
        TestLocator.locatedKeys.clear();
        Guice.createInjector( new BeanImportModule( new TestModule() ) );
        System.out.println( TestLocator.locatedKeys );
    }

    public void testDuplicatesAreIgnored()
    {
        Guice.createInjector( new BeanImportModule( new TestModule(), new TestModule() ) );
    }

    public void testInvalidTypeParameters()
    {
        TestLocator.locatedKeys.clear();
        try
        {
            Guice.createInjector( new BeanImportModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "BM" ) ).to( BadMap.class );
                    bind( X.class ).annotatedWith( Names.named( "RL" ) ).to( RawList.class );
                    bind( X.class ).annotatedWith( Names.named( "RM" ) ).to( RawMap.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }
        assertTrue( TestLocator.locatedKeys.isEmpty() );
    }
}
