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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class EntryMapAdapterTest
    extends TestCase
{
    @SuppressWarnings( "boxing" )
    public void testMapSize()
    {
        final Map<String, Integer> original = new HashMap<String, Integer>();
        final Map<String, Integer> adapter = new EntryMapAdapter<String, Integer>( original.entrySet() );

        assertTrue( adapter.isEmpty() );
        original.put( "A", 1 );
        assertFalse( adapter.isEmpty() );

        assertEquals( 1, adapter.size() );
        original.put( "C", 3 );
        assertEquals( 2, adapter.size() );
        original.put( "B", 2 );
        assertEquals( 3, adapter.size() );
        original.remove( "C" );
        assertEquals( 2, adapter.size() );
    }

    @SuppressWarnings( "boxing" )
    public void testMapEquality()
    {
        final Map<Integer, String> original = new HashMap<Integer, String>();
        final Map<Integer, String> adapter = new EntryMapAdapter<Integer, String>( original.entrySet() );

        assertEquals( original, adapter );
        original.put( 3, "C" );
        assertEquals( original, adapter );
        original.put( 1, "A" );
        assertEquals( original, adapter );
        original.put( 2, "B" );
        assertEquals( original, adapter );
        original.clear();
        assertEquals( original, adapter );
    }
}
