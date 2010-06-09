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

import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import junit.framework.TestCase;

import org.sonatype.inject.BeanMediator;
import org.sonatype.inject.NamedBeanMediator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class WatchedBeansTest
    extends TestCase
{
    interface Bean
    {
    }

    static class BeanImpl
        implements Bean
    {
    }

    Injector parent;

    Injector child1;

    Injector child2;

    Injector child3;

    @Override
    public void setUp()
        throws Exception
    {
        parent = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "-" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "Z" ) ).to( BeanImpl.class );
            }
        } );

        child1 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M1" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "N1" ) ).to( BeanImpl.class );
            }
        } );

        child2 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
            }
        } );

        child3 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M3" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "N3" ) ).to( BeanImpl.class );
            }
        } );
    }

    static class NoOpMediator
        implements BeanMediator<Named, Bean, Object>
    {
        public void add( final Named name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
        }

        public void remove( final Named name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
        }
    }

    static class BrokenMediator
        implements BeanMediator<Named, Bean, Object>
    {
        public void add( final Named name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
            throw new Exception();
        }

        public void remove( final Named name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
            throw new Exception();
        }
    }

    static class TrackingMediator
        implements BeanMediator<Named, Bean, Object>
    {
        final Map<String, Bean> beans = new HashMap<String, Bean>();

        public void add( final Named name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
            assertNull( beans.put( name.value(), bean.get() ) );
        }

        public void remove( final Named name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
            assertSame( beans.remove( name.value() ), bean.get() );
        }
    }

    static class NamedTrackingMediator
        implements NamedBeanMediator<Bean, Object>
    {
        final Map<String, Bean> beans = new HashMap<String, Bean>();

        public void add( final String name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
            assertNull( beans.put( name, bean.get() ) );
        }

        public void remove( final String name, final Provider<Bean> bean, final Object watcher )
            throws Exception
        {
            assertSame( beans.remove( name ), bean.get() );
        }
    }

    public void testOptionalLogging()
        throws Exception
    {
        final Level level = Logger.getLogger( "" ).getLevel();
        try
        {
            Logger.getLogger( "" ).setLevel( Level.SEVERE );

            // check everything still works without any SLF4J jars
            final ClassLoader noLoggingLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "slf4j" ) )
                        {
                            throw new ClassNotFoundException( name );
                        }
                        if ( name.contains( "cobertura" ) )
                        {
                            return WatchedBeansTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noLoggingLoader.loadClass( BrokenMediationExample.class.getName() ).newInstance();
        }
        finally
        {
            Logger.getLogger( "" ).setLevel( level );
        }
    }

    public void testWeakMediation()
        throws Exception
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Field mediatedWatchersField = DefaultBeanLocator.class.getDeclaredField( "exposedBeans" );
        mediatedWatchersField.setAccessible( true );

        final List<?> mediatedWatchers = (List<?>) mediatedWatchersField.get( locator );

        final BeanMediator<Named, Bean, Object> noOpMediator = new NoOpMediator();
        final BeanMediator<Named, Bean, Object> brokenMediator = new BrokenMediator();

        final BeanMediator<Named, Bean, Object> trackingMediator1 = new TrackingMediator();
        final BeanMediator<Named, Bean, Object> trackingMediator2 =
            new NamedBeanMediatorAdapter<Bean, Object>( new NamedTrackingMediator() );

        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        assertEquals( 0, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), noOpMediator, a );
        assertEquals( 1, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), brokenMediator, b );
        assertEquals( 2, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), trackingMediator1, c );
        assertEquals( 3, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), trackingMediator2, c );
        assertEquals( 4, mediatedWatchers.size() );

        forceGC();

        assertEquals( 4, mediatedWatchers.size() );
        locator.add( child1 );
        locator.add( child2 );
        locator.remove( child1 );
        assertEquals( 4, mediatedWatchers.size() );

        b = null;
        forceGC();

        assertEquals( 4, mediatedWatchers.size() );
        locator.remove( child2 );
        locator.add( child1 );
        assertEquals( 3, mediatedWatchers.size() );

        a = null;
        forceGC();

        assertEquals( 3, mediatedWatchers.size() );
        locator.add( child2 );
        assertEquals( 2, mediatedWatchers.size() );

        c = null;
        forceGC();

        assertEquals( 2, mediatedWatchers.size() );
        locator.remove( child1 );
        locator.add( child1 );
        assertEquals( 0, mediatedWatchers.size() );

        a = new Object();

        assertEquals( 0, mediatedWatchers.size() );
        locator.watch( Key.get( Bean.class, Named.class ), noOpMediator, a );
        assertEquals( 1, mediatedWatchers.size() );

        QualifiedBeans<Named, Bean> beans;

        a = new Object();
        beans = new WatchedBeans<Named, Bean, Object>( Key.get( Bean.class, Named.class ), noOpMediator, a );
        beans.add( parent );
        a = null;
        forceGC();
        beans.add( child1 );

        a = new Object();
        beans = new WatchedBeans<Named, Bean, Object>( Key.get( Bean.class, Named.class ), noOpMediator, a );
        beans.add( parent );
        a = null;
        forceGC();
        beans.remove( parent );
    }

    private static String[] forceGC()
    {
        String[] buf;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        buf = new String[8 * 1024 * 1024];
        buf = null;
        System.gc();
        return buf;
    }
}
