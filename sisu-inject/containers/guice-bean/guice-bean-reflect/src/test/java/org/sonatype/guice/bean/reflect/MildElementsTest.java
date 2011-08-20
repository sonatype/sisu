/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class MildElementsTest
    extends TestCase
{
    public void testWeakElements()
    {
        final Collection<String> names = new MildElements<String>( new ArrayList<Reference<String>>(), false );

        String a = new String( "A" ), b = new String( "B" ), c = new String( "C" );

        assertTrue( names.isEmpty() );
        assertEquals( 0, names.size() );

        names.add( a );

        assertFalse( names.isEmpty() );
        assertEquals( 1, names.size() );

        names.add( b );

        assertFalse( names.isEmpty() );
        assertEquals( 2, names.size() );

        names.add( c );

        assertFalse( names.isEmpty() );
        assertEquals( 3, names.size() );

        Iterator<String> itr = names.iterator();

        assertTrue( itr.hasNext() );
        assertEquals( "A", itr.next() );
        assertEquals( "B", itr.next() );
        itr.remove();
        assertTrue( itr.hasNext() );
        assertEquals( "C", itr.next() );
        assertFalse( itr.hasNext() );

        names.add( b = new String( "b2b" ) );

        itr = names.iterator();

        assertEquals( "A", itr.next() );
        assertEquals( "C", itr.next() );
        assertEquals( "b2b", itr.next() );

        try
        {
            itr.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        try
        {
            itr.remove();
            fail( "Expected IllegalStateException" );
        }
        catch ( final IllegalStateException e )
        {
        }

        char[] buf;

        c = null; // clear so element can be evicted

        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();

        itr = names.iterator();

        assertEquals( "A", itr.next() );
        assertEquals( "b2b", itr.next() );
        assertFalse( itr.hasNext() );
        itr = null;

        a = null; // clear so element can be evicted

        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();

        itr = names.iterator();

        assertEquals( "b2b", itr.next() );
        assertFalse( itr.hasNext() );
        itr = null;

        b = null; // clear so element can be evicted

        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();
        buf = new char[8 * 1024 * 1024];
        System.gc();

        itr = names.iterator();

        assertFalse( itr.hasNext() );

        buf.toString();
    }
}
