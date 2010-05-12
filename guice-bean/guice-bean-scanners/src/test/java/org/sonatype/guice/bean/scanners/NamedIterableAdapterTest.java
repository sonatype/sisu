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
package org.sonatype.guice.bean.scanners;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.EntryMapAdapter;

import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class NamedIterableAdapterTest
    extends TestCase
{
    public void testNamedAdapter()
    {
        final Map<Named, String> original = new LinkedHashMap<Named, String>();

        final Map<String, String> adapter =
            new EntryMapAdapter<String, String>( new NamedIterableAdapter<String>( original.entrySet() ) );

        assertEquals( original, adapter );
        original.put( Names.named( "3" ), "C" );
        assertEquals( original, adapter );
        original.put( Names.named( "1" ), "A" );
        assertEquals( original, adapter );
        original.put( Names.named( "2" ), "B" );
        assertEquals( original, adapter );
        original.clear();
        assertEquals( original, adapter );
    }

    private static void assertEquals( final Map<Named, String> named, final Map<String, String> hinted )
    {
        final Iterator<Entry<Named, String>> i = named.entrySet().iterator();
        final Iterator<Entry<String, String>> j = hinted.entrySet().iterator();
        while ( i.hasNext() )
        {
            assertTrue( j.hasNext() );
            final Entry<Named, String> lhs = i.next();
            final Entry<String, String> rhs = j.next();
            assertEquals( lhs.getKey().value(), rhs.getKey() );
            assertEquals( lhs.getValue(), rhs.getValue() );
        }
        assertFalse( j.hasNext() );
    }

    public void testUnsupportedOperations()
    {
        final Map<Named, String> original = new LinkedHashMap<Named, String>();

        final Map<String, String> adapter =
            new EntryMapAdapter<String, String>( new NamedIterableAdapter<String>( original.entrySet() ) );

        original.put( Names.named( "1" ), "A" );

        try
        {
            adapter.entrySet().iterator().remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        try
        {
            adapter.entrySet().iterator().next().setValue( "B" );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }
}
