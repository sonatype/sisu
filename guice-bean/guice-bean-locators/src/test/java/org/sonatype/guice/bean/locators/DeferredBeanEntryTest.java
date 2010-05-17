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

import javax.inject.Provider;

import junit.framework.TestCase;

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
            return null;
        }
    }

    public void testGetContention()
    {
        final Entry<Annotation, Object> countingEntry =
            new DeferredBeanEntry<Annotation, Object>( null, new CountingProvider() );

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

    public void testHashCodeEquals()
    {
        final Entry<Named, String> lhs = new DeferredBeanEntry<Named, String>( Names.named( "lhs" ), null );
        final Entry<Named, String> rhs = new DeferredBeanEntry<Named, String>( Names.named( "rhs" ), null );

        assertTrue( lhs.equals( lhs ) );
        assertTrue( rhs.equals( rhs ) );

        assertFalse( lhs.equals( rhs ) );
        assertFalse( rhs.equals( lhs ) );

        assertFalse( lhs.equals( "lhs" ) );
        assertFalse( rhs.equals( "rhs" ) );

        assertEquals( Names.named( "lhs" ).hashCode(), lhs.hashCode() );
        assertEquals( Names.named( "rhs" ).hashCode(), rhs.hashCode() );
    }

    public void testToString()
    {
        final Entry<Named, String> textEntry =
            new DeferredBeanEntry<Named, String>( Names.named( "KEY" ), new Provider<String>()
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
            } );

        assertEquals( Names.named( "KEY" ) + "=VALUE", textEntry.toString() );
    }
}
