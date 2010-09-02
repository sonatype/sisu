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

import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class NotifyingBeansTest
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

    static class NoOpNotifier
        implements Runnable
    {
        public void run()
        {
        }
    }

    static class BrokenNotifier
        implements Runnable
    {
        public void run()
        {
            throw new RuntimeException();
        }
    }

    static class CountingNotifier
        implements Runnable
    {
        int count;

        public void run()
        {
            count++;
        }
    }

    @SuppressWarnings( "unused" )
    public void testNotification()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final NoOpNotifier noOpNotifier = new NoOpNotifier();
        final BrokenNotifier brokenNotifier = new BrokenNotifier();
        final CountingNotifier countingNotifier = new CountingNotifier();

        final Iterable<QualifiedBean<Named, Bean>> a =
            locator.locate( Key.get( Bean.class, Named.class ), noOpNotifier );
        final Iterable<QualifiedBean<Named, Bean>> b =
            locator.locate( Key.get( Bean.class, Named.class ), brokenNotifier );
        final Iterable<QualifiedBean<Named, Bean>> c =
            locator.locate( Key.get( Bean.class, Named.class ), countingNotifier );

        assertEquals( 0, countingNotifier.count );
        locator.add( child1 );
        assertEquals( 1, countingNotifier.count );
        locator.add( child2 );
        assertEquals( 1, countingNotifier.count );
        locator.add( child1 );
        assertEquals( 1, countingNotifier.count );
        locator.add( child2 );
        assertEquals( 1, countingNotifier.count );
        locator.remove( child1 );
        assertEquals( 2, countingNotifier.count );
        locator.remove( child2 );
        assertEquals( 2, countingNotifier.count );
        locator.remove( child1 );
        assertEquals( 2, countingNotifier.count );
        locator.add( child3 );
        assertEquals( 3, countingNotifier.count );
        locator.add( child2 );
        assertEquals( 3, countingNotifier.count );
        locator.clear();
        assertEquals( 4, countingNotifier.count );
        locator.add( child2 );
        locator.clear();
        assertEquals( 4, countingNotifier.count );
        locator.remove( child3 );
        locator.remove( child2 );
        locator.remove( child1 );
        assertEquals( 4, countingNotifier.count );
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
                            return NotifyingBeansTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noLoggingLoader.loadClass( BrokenNotifierExample.class.getName() ).newInstance();
        }
        finally
        {
            Logger.getLogger( "" ).setLevel( level );
        }
    }
}
