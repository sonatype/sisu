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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EventListener;
import java.util.RandomAccess;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.inject.Mediator;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class QualifiedTypesTest
    extends TestCase
{
    @Named
    static class DefaultB01
    {
    }

    @Named( "default" )
    static class B01
        extends Thread
    {
    }

    @Named
    @Typed( EventListener.class )
    static class DefaultB02
        implements RandomAccess, EventListener
    {
    }

    @Named
    @Typed( EventListener.class )
    static class B02
        implements RandomAccess, EventListener
    {
    }

    @Named
    static class B03
        implements EventListener
    {
    }

    @Named
    @EagerSingleton
    static class DefaultB03
    {
        static boolean initialized;

        @Inject
        void initialize()
        {
            initialized = true;
        }
    }

    @Named( "TEST" )
    static class DefaultB04
    {
    }

    @Named( "TEST" )
    static class B04
    {
    }

    @Named
    static class B05EventListener
        implements Runnable, EventListener, Serializable
    {
        private static final long serialVersionUID = 1L;

        public void run()
        {
        }
    }

    @Named
    @Typed( { EventListener.class, Runnable.class } )
    static class B06
        implements Runnable, EventListener, Serializable
    {
        private static final long serialVersionUID = 1L;

        public void run()
        {
        }
    }

    static abstract class AbstractB01
    {
    }

    @Typed( EventListener.class )
    static abstract class AbstractB02
        implements RandomAccess, EventListener
    {
    }

    static abstract class AbstractB03
        implements EventListener
    {
    }

    @Named
    static class SubclassB00
        extends B01
    {
    }

    @Named
    static class SubclassB01
        extends AbstractB01
    {
    }

    @Named
    static class SubclassB02
        extends AbstractB02
    {
    }

    @Named
    static class SubclassB03EventListener
        extends AbstractB03
    {
    }

    @Named( "RENAME" )
    static class SubclassB04EventListener
        extends AbstractB03
    {
    }

    @Named
    static class SubclassB06
        extends B02
    {
    }

    @Named
    @Typed( Serializable.class )
    static class SubclassB07
        extends B02
        implements Runnable, Serializable
    {
        private static final long serialVersionUID = 1L;

        public void run()
        {
        }
    }

    @Named
    static class SubclassB08
        extends B02
        implements Serializable
    {
        private static final long serialVersionUID = 1L;
    }

    @Qualifier
    @Retention( RetentionPolicy.RUNTIME )
    public @interface Legacy
    {
    }

    static class LegacyImpl
        implements Legacy
    {
        public Class<? extends Annotation> annotationType()
        {
            return Legacy.class;
        }
    }

    @Legacy
    static class LegacyRunnable
        implements Runnable
    {
        public void run()
        {
        }
    }

    private BeanLocator locator;

    private Injector injector;

    @Override
    protected void setUp()
        throws Exception
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        injector = Guice.createInjector( new SpaceModule( space ) );
        locator = injector.getInstance( BeanLocator.class );

        assertTrue( DefaultB03.initialized );
    }

    private void checkDefaultBinding( final Class<?> api, final Class<?> imp )
    {
        assertEquals( imp, injector.getInstance( Key.get( api ) ).getClass() );

        final Annotation defaultName = Names.named( "default" );

        assertEquals( imp, locator.locate( Key.get( api ), null ).iterator().next().getValue().getClass() );
        assertEquals( imp, locator.locate( Key.get( api, Named.class ), null ).iterator().next().getValue().getClass() );
        assertEquals( imp, locator.locate( Key.get( api, defaultName ), null ).iterator().next().getValue().getClass() );
    }

    private void checkNamedBinding( final Class<?> api, final String name, final Class<?> imp )
    {
        assertEquals( imp, injector.getInstance( Key.get( api, Names.named( name ) ) ).getClass() );
    }

    private void checkLegacyBinding( final Class<?> api, final Class<?> imp )
    {
        assertEquals( imp, injector.getInstance( Key.get( api, Names.named( imp.getName() ) ) ).getClass() );
    }

    public void testQualifiedBindings()
    {
        checkDefaultBinding( DefaultB01.class, DefaultB01.class );
        checkDefaultBinding( DefaultB03.class, DefaultB03.class );
        checkDefaultBinding( DefaultB04.class, DefaultB04.class );

        checkDefaultBinding( Thread.class, B01.class );
        checkDefaultBinding( B04.class, B04.class );

        checkNamedBinding( EventListener.class, DefaultB02.class.getName(), DefaultB02.class );

        checkNamedBinding( EventListener.class, B02.class.getName(), B02.class );
        checkNamedBinding( EventListener.class, B03.class.getName(), B03.class );

        checkNamedBinding( EventListener.class, B05EventListener.class.getName(), B05EventListener.class );
        checkNamedBinding( EventListener.class, B06.class.getName(), B06.class );

        checkNamedBinding( B01.class, SubclassB00.class.getName(), SubclassB00.class );
        checkNamedBinding( AbstractB01.class, SubclassB01.class.getName(), SubclassB01.class );

        checkNamedBinding( AbstractB02.class, SubclassB02.class.getName(), SubclassB02.class );
        checkNamedBinding( EventListener.class, SubclassB03EventListener.class.getName(),
                           SubclassB03EventListener.class );
        checkNamedBinding( EventListener.class, "RENAME", SubclassB04EventListener.class );
        checkNamedBinding( B02.class, SubclassB06.class.getName(), SubclassB06.class );

        checkNamedBinding( Serializable.class, SubclassB07.class.getName(), SubclassB07.class );
        checkNamedBinding( Serializable.class, SubclassB08.class.getName(), SubclassB08.class );

        checkLegacyBinding( Runnable.class, LegacyRunnable.class );
    }

    @ImplementedBy( AImpl.class )
    interface A
    {
    }

    static class AImpl
        implements A
    {
    }

    static class Ambiguous
        implements Serializable, EventListener
    {
        private static final long serialVersionUID = 1L;
    }

    @SuppressWarnings( "rawtypes" )
    static class RawMediator
        implements Mediator
    {
        public void add( final Object qualifier, final Provider bean, final Object watcher )
            throws Exception
        {
        }

        public void remove( final Object qualifier, final Provider bean, final Object watcher )
            throws Exception
        {
        }
    }

    abstract class AbstractNamedMediator
        implements Mediator<Named, Object, Object>
    {
    }

    abstract class AbstractStringMediator
        implements Mediator<String, Object, Object>
    {
    }

    static class BadBindings
        implements Module
    {
        public void configure( final Binder binder )
        {
            final QualifiedTypeListener listener = new QualifiedTypeBinder( binder );

            listener.hear( Names.named( "ambiguous" ), Ambiguous.class, null );

            listener.hear( Names.named( "raw" ), RawMediator.class, null );

            listener.hear( Names.named( "abstract" ), AbstractNamedMediator.class, null );
            listener.hear( Names.named( "abstract" ), AbstractStringMediator.class, null );
            listener.hear( Names.named( "abstract" ), AbstractModule.class, null );
        }
    }

    public void testBadBindings()
    {
        try
        {
            Guice.createInjector( new BadBindings() );
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }
}
