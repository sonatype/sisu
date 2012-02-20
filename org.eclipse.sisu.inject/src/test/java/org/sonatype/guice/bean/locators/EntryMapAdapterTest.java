/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
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
