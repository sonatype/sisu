/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.reflect;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class ZipEntryIteratorTest
    extends TestCase
{
    public void testNonJar()
        throws IOException
    {
        final Iterator<String> i = new ZipEntryIterator( new URL( "file:" ) );
        assertFalse( i.hasNext() );
        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testBlankZip()
    {
        final Iterator<String> i = new ZipEntryIterator( resource( "blank.zip" ) );
        assertFalse( i.hasNext() );
        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testEmptyZip()
    {
        final Iterator<String> i = new ZipEntryIterator( resource( "empty.zip" ) );
        assertFalse( i.hasNext() );
        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testEmptyJar()
    {
        final Iterator<String> i = new ZipEntryIterator( resource( "empty.jar" ) );
        assertTrue( i.hasNext() );
        assertEquals( "META-INF/MANIFEST.MF", i.next() );
        assertFalse( i.hasNext() );
        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testSimpleZip()
    {
        final Iterator<String> i = new ZipEntryIterator( resource( "simple.zip" ) );
        assertEquals( "0", i.next() );
        assertEquals( "a/1", i.next() );
        assertEquals( "a/b/2", i.next() );
        assertEquals( "a/b/c/3", i.next() );
        assertEquals( "4", i.next() );
        assertEquals( "x/5", i.next() );
        assertEquals( "x/y/6", i.next() );
        assertEquals( "7", i.next() );
        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testSimpleJar()
    {
        final Iterator<String> i = new ZipEntryIterator( resource( "simple.jar" ) );
        assertEquals( "META-INF/", i.next() );
        assertEquals( "META-INF/MANIFEST.MF", i.next() );
        assertEquals( "0", i.next() );
        assertEquals( "a/", i.next() );
        assertEquals( "a/1", i.next() );
        assertEquals( "a/b/", i.next() );
        assertEquals( "a/b/2", i.next() );
        assertEquals( "a/b/c/", i.next() );
        assertEquals( "a/b/c/3", i.next() );
        assertEquals( "4", i.next() );
        assertEquals( "x/", i.next() );
        assertEquals( "x/5", i.next() );
        assertEquals( "x/y/", i.next() );
        assertEquals( "x/y/6", i.next() );
        assertEquals( "7", i.next() );
        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testEmbeddedZip()
        throws MalformedURLException
    {
        final Iterator<String> i =
            new ZipEntryIterator( new URL( "jar:" + resource( "embedded.zip" ) + "!/simple.zip" ) );

        assertEquals( "0", i.next() );
        assertEquals( "a/1", i.next() );
        assertEquals( "a/b/2", i.next() );
        assertEquals( "a/b/c/3", i.next() );
        assertEquals( "4", i.next() );
        assertEquals( "x/5", i.next() );
        assertEquals( "x/y/6", i.next() );
        assertEquals( "7", i.next() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testBrokenJar()
    {
        final Iterator<String> i = new ZipEntryIterator( resource( "broken.jar" ) );
        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testRemoveNotSupported()
        throws IOException
    {
        final Iterator<String> i = new ZipEntryIterator( new URL( "file:" ) );
        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    private static URL resource( final String name )
    {
        return ZipEntryIteratorTest.class.getClassLoader().getResource( name );
    }
}
