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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class DeferredBeanEntryTest
    extends TestCase
{
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
            new DeferredBeanEntry<Annotation, Object>( injector.getBinding( Object.class ) );

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

    static class ToStringProvider
        implements Provider<String>
    {
        public String get()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString()
        {
            return "VALUE";
        }
    }

    public void testToString()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( String.class ).annotatedWith( Names.named( "KEY" ) ).toProvider( new ToStringProvider() );
            }
        } );

        final Entry<Named, String> textEntry =
            new DeferredBeanEntry<Named, String>( injector.getBinding( Key.get( String.class, Names.named( "KEY" ) ) ) );

        assertEquals( Names.named( "KEY" ) + "=VALUE", textEntry.toString() );
    }
}
