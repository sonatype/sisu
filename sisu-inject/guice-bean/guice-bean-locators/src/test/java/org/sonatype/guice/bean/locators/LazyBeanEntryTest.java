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
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.RankedBindingsTest.Bean;
import org.sonatype.guice.bean.locators.RankedBindingsTest.BeanImpl;
import org.sonatype.inject.Description;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

public class LazyBeanEntryTest
    extends TestCase
{
    @Description( "This is a test" )
    static class DescribedBean
        implements Bean
    {
    }

    public void testDetails()
    {
        final Key<Bean> key1 = Key.get( Bean.class, Names.named( "1" ) );
        final Key<Bean> key2 = Key.get( Bean.class, Names.named( "2" ) );
        final Key<Bean> key3 = Key.get( Bean.class, Names.named( "3" ) );
        final Key<Bean> key4 = Key.get( Bean.class, Names.named( "4" ) );

        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( key1 ).to( DescribedBean.class ).in( Scopes.SINGLETON );

                binder().withSource( new BeanDescription()
                {
                    public String getDescription()
                    {
                        return "Another test";
                    }
                } ).bind( key2 ).toInstance( new BeanImpl() );

                binder().withSource( "where?" ).bind( key3 ).to( BeanImpl.class );

                bind( key4 ).toProvider( Providers.of( new BeanImpl() ) );
            }
        } );

        final LazyBeanEntry<Annotation, Bean> bean1 =
            new LazyBeanEntry<Annotation, Bean>( key1.getAnnotation(), injector.getBinding( key1 ), 42 );
        final LazyBeanEntry<Annotation, Bean> bean2 =
            new LazyBeanEntry<Annotation, Bean>( key2.getAnnotation(), injector.getBinding( key2 ), -24 );
        final LazyBeanEntry<Annotation, Bean> bean3 =
            new LazyBeanEntry<Annotation, Bean>( key3.getAnnotation(), injector.getBinding( key3 ), 0 );
        final LazyBeanEntry<Annotation, Bean> bean4 =
            new LazyBeanEntry<Annotation, Bean>( key4.getAnnotation(), injector.getBinding( key4 ), -1 );

        assertEquals( "This is a test", bean1.getDescription() );
        assertTrue( bean1.getSource() instanceof StackTraceElement );
        assertEquals( DescribedBean.class, bean1.getImplementationClass() );
        assertEquals( 42, bean1.getRank() );

        assertEquals( "Another test", bean2.getDescription() );
        assertTrue( bean2.getSource() instanceof BeanDescription );
        assertEquals( BeanImpl.class, bean2.getImplementationClass() );
        assertEquals( -24, bean2.getRank() );

        assertNull( bean3.getDescription() );
        assertTrue( bean3.getSource() instanceof String );
        assertEquals( BeanImpl.class, bean3.getImplementationClass() );
        assertEquals( 0, bean3.getRank() );

        assertNull( bean4.getDescription() );
        assertTrue( bean4.getSource() instanceof StackTraceElement );
        assertEquals( null, bean4.getImplementationClass() );
        assertEquals( -1, bean4.getRank() );
    }

    static class CountingProvider
        implements Provider<Object>
    {
        static int count;

        public Object get()
        {
            count++;
            return "";
        }
    }

    public void testGetContention()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Object.class ).toProvider( CountingProvider.class );
            }
        } );

        final Entry<Annotation, Object> countingEntry =
            new LazyBeanEntry<Annotation, Object>( null, injector.getBinding( Object.class ), 0 );

        final Thread[] pool = new Thread[8];
        for ( int i = 0; i < pool.length; i++ )
        {
            pool[i] = new Thread()
            {
                @Override
                public void run()
                {
                    countingEntry.getValue();
                }
            };
        }

        for ( final Thread thread : pool )
        {
            thread.start();
        }

        for ( final Thread thread : pool )
        {
            try
            {
                thread.join();
            }
            catch ( final InterruptedException e )
            {
            }
        }

        assertEquals( 1, CountingProvider.count );

        try
        {
            countingEntry.setValue( null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    @javax.inject.Named( "TEST" )
    interface T
    {
    }

    public void testJsrNamed()
    {
        final Named guiceNamed = Names.named( "TEST" );

        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindConstant().annotatedWith( guiceNamed ).to( "CONSTANT" );
            }
        } );

        @SuppressWarnings( { "unchecked", "rawtypes" } )
        final LazyBeanEntry<javax.inject.Named, String> entry =
            new LazyBeanEntry( guiceNamed, injector.getBinding( Key.get( String.class, guiceNamed ) ), 0 );

        final javax.inject.Named jsrNamed = entry.getKey();

        assertTrue( jsrNamed.equals( jsrNamed ) );
        assertTrue( jsrNamed.equals( entry.getKey() ) );
        assertTrue( jsrNamed.equals( T.class.getAnnotation( javax.inject.Named.class ) ) );
        assertTrue( jsrNamed.equals( guiceNamed ) );

        assertFalse( jsrNamed.equals( Names.named( "" ) ) );
        assertFalse( jsrNamed.equals( "TEST" ) );

        assertEquals( javax.inject.Named.class, jsrNamed.annotationType() );

        assertEquals( T.class.getAnnotation( javax.inject.Named.class ).hashCode(), jsrNamed.hashCode() );
    }

    static class ToStringProvider
        implements Provider<String>
    {
        public String get()
        {
            return "VALUE";
        }
    }

    static class BadToStringProvider
        implements Provider<String>
    {
        public String get()
        {
            throw new ProvisionException( "OOPS" );
        }
    }

    public void testToString()
    {
        final Key<String> key1 = Key.get( String.class, Names.named( "KEY" ) );
        final Key<String> key2 = Key.get( String.class, Names.named( "BAD" ) );

        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( key1 ).toProvider( new ToStringProvider() );
                bind( key2 ).toProvider( new BadToStringProvider() );
            }
        } );

        final Entry<Named, String> entry1 =
            new LazyBeanEntry<Named, String>( (Named) key1.getAnnotation(), injector.getBinding( key1 ), 0 );
        final Entry<Named, String> entry2 =
            new LazyBeanEntry<Named, String>( (Named) key2.getAnnotation(), injector.getBinding( key2 ), 0 );

        Exception error = null;
        try
        {
            injector.getInstance( key2 );
        }
        catch ( final Exception e )
        {
            error = e;
        }

        assertEquals( '@' + javax.inject.Named.class.getName() + "(value=KEY)=VALUE", entry1.toString() );
        assertEquals( '@' + javax.inject.Named.class.getName() + "(value=BAD)=" + error, entry2.toString() );
    }
}
