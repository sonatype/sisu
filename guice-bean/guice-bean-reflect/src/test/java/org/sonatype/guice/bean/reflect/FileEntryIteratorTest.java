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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

public class FileEntryIteratorTest
    extends TestCase
{
    public void testURLtoFile()
        throws MalformedURLException
    {
        assertEquals( "test", FileEntryIterator.toFile( new URL( "file:test" ) ).getPath() );
        assertEquals( "A B C", FileEntryIterator.toFile( new URL( "file:A B C" ) ).getPath() );
        assertEquals( "A B C%%", FileEntryIterator.toFile( new URL( "file:A B%20C%%" ) ).getPath() );
    }

    public void testNoSuchFile()
        throws Exception
    {
        final Iterator<String> i = new FileEntryIterator( new URL( "file:UNKNOWN" ), null, true );
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

    public void testEmptyFolder()
        throws Exception
    {
        final Iterator<String> i = new FileEntryIterator( expand( resource( "empty.zip" ) ), null, true );
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

    public void testTrivialFolder()
        throws Exception
    {
        final Iterator<String> i = new FileEntryIterator( expand( resource( "empty.jar" ) ), null, true );
        assertTrue( i.hasNext() );
        assertEquals( "META-INF/", i.next() );
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

    public void testSimpleFolder()
        throws Exception
    {
        final Iterator<String> i = new FileEntryIterator( expand( resource( "simple.jar" ) ), null, true );

        final Set<String> names = new HashSet<String>();
        while ( i.hasNext() )
        {
            names.add( i.next() );
        }

        assertTrue( names.remove( "META-INF/" ) );
        assertTrue( names.remove( "META-INF/MANIFEST.MF" ) );
        assertTrue( names.remove( "0" ) );
        assertTrue( names.remove( "a/" ) );
        assertTrue( names.remove( "a/1" ) );
        assertTrue( names.remove( "a/b/" ) );
        assertTrue( names.remove( "a/b/2" ) );
        assertTrue( names.remove( "a/b/c/" ) );
        assertTrue( names.remove( "a/b/c/3" ) );
        assertTrue( names.remove( "4" ) );
        assertTrue( names.remove( "x/" ) );
        assertTrue( names.remove( "x/5" ) );
        assertTrue( names.remove( "x/y/" ) );
        assertTrue( names.remove( "x/y/6" ) );
        assertTrue( names.remove( "7" ) );

        assertTrue( names.isEmpty() );
    }

    public void testNoRecursion()
        throws Exception
    {
        final Iterator<String> i = new FileEntryIterator( expand( resource( "simple.jar" ) ), null, false );

        final Set<String> names = new HashSet<String>();
        while ( i.hasNext() )
        {
            names.add( i.next() );
        }

        assertTrue( names.remove( "META-INF/" ) );
        assertTrue( names.remove( "0" ) );
        assertTrue( names.remove( "a/" ) );
        assertTrue( names.remove( "4" ) );
        assertTrue( names.remove( "x/" ) );
        assertTrue( names.remove( "7" ) );

        assertTrue( names.isEmpty() );
    }

    public void testSubPath()
        throws Exception
    {
        final Iterator<String> i = new FileEntryIterator( expand( resource( "simple.jar" ) ), "a/b", true );

        final Set<String> names = new HashSet<String>();
        while ( i.hasNext() )
        {
            names.add( i.next() );
        }

        assertTrue( names.remove( "a/b/2" ) );
        assertTrue( names.remove( "a/b/c/" ) );
        assertTrue( names.remove( "a/b/c/3" ) );

        assertTrue( names.isEmpty() );
    }

    public void testRemoveNotSupported()
        throws IOException
    {
        final Iterator<String> i = new FileEntryIterator( new URL( "file:" ), null, false );
        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    static URL expand( final URL url )
        throws Exception
    {
        final File jar = new File( url.toURI() );
        final File dir = new File( jar.getParentFile(), jar.getName() + "_expanded" );

        try
        {
            final ZipFile zip = new ZipFile( jar );
            for ( final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); )
            {
                final ZipEntry entry = e.nextElement();
                final File path = new File( dir, entry.getName() );
                if ( entry.isDirectory() )
                {
                    path.mkdirs();
                }
                else
                {
                    path.getParentFile().mkdirs();
                    final ReadableByteChannel in = Channels.newChannel( zip.getInputStream( entry ) );
                    final FileChannel out = new FileOutputStream( path ).getChannel();
                    out.transferFrom( in, 0, entry.getSize() );
                    out.close();
                    in.close();
                }
            }
        }
        catch ( final IOException e )
        {
            System.out.println( e + " " + jar.getName() );
        }

        return dir.toURI().toURL();
    }

    private static URL resource( final String name )
    {
        return FileEntryIteratorTest.class.getClassLoader().getResource( name );
    }
}
