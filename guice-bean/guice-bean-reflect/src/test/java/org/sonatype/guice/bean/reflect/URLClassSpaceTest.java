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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Manifest;

import junit.framework.TestCase;

public class URLClassSpaceTest
    extends TestCase
{
    private static final ClassLoader TEST_LOADER = URLClassSpaceTest.class.getClassLoader();

    private static final URL SIMPLE_JAR = TEST_LOADER.getResource( "simple.jar" );

    private static final URL CLASS_PATH_JAR = TEST_LOADER.getResource( "class path.jar" );

    private static final URL COMMONS_LOGGING_JAR = TEST_LOADER.getResource( "commons-logging-1.1.1.jar" );

    private static final URL CORRUPT_MANIFEST = TEST_LOADER.getResource( "corrupt.manifest/" );

    public void testHashCodeAndEquals()
    {
        final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        final ClassSpace space = new URLClassSpace( systemLoader, null );

        assertEquals( space, space );

        assertEquals( space, new URLClassSpace( systemLoader, new URL[] { SIMPLE_JAR } ) );

        assertFalse( space.equals( new ClassSpace()
        {
            public Class<?> loadClass( final String name )
                throws ClassNotFoundException
            {
                return space.loadClass( name );
            }

            public Enumeration<URL> getResources( final String name )
                throws IOException
            {
                return space.getResources( name );
            }

            public URL getResource( final String name )
            {
                return space.getResource( name );
            }

            public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
                throws IOException
            {
                return space.findEntries( path, glob, recurse );
            }

            public DeferredClass<?> deferLoadClass( final String name )
            {
                return space.deferLoadClass( name );
            }
        } ) );

        assertEquals( systemLoader.hashCode(), space.hashCode() );
        assertEquals( systemLoader.toString(), space.toString() );
    }

    public void testClassSpaceResources()
        throws IOException
    {
        final ClassSpace space = new URLClassSpace( URLClassLoader.newInstance( new URL[] { COMMONS_LOGGING_JAR } ) );
        Enumeration<URL> e;

        int n = 0;
        e = space.getResources( "META-INF/MANIFEST.MF" );
        while ( true )
        {
            n++;

            // should have several matches from parent loader, local match should be last
            if ( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) )
            {
                assertFalse( e.hasMoreElements() );
                break;
            }
        }
        assertTrue( n > 1 );

        e = space.findEntries( "META-INF", "*.MF", false );

        // only expect to see single result
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) );
        assertFalse( e.hasMoreElements() );

        final URL manifestURL = space.getResource( "META-INF/MANIFEST.MF" );
        assertNotNull( manifestURL );
        new Manifest( manifestURL.openStream() );
    }

    public void testClassPathExpansion()
        throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", getClass().getPackage().getName() );

        final ClassSpace space =
            new URLClassSpace( URLClassLoader.newInstance( new URL[] { SIMPLE_JAR, CLASS_PATH_JAR, null,
                new URL( "barf:up/" ), CLASS_PATH_JAR, CORRUPT_MANIFEST } ) );

        final Enumeration<URL> e = space.findEntries( "META-INF", "*.MF", false );

        // expect to see three results
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( SIMPLE_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( CLASS_PATH_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) );
        assertFalse( e.hasMoreElements() );
    }

    public void testNullSearchPath()
        throws IOException
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader(), null );
        final Enumeration<URL> e = space.findEntries( null, null, true );

        // local search should see nothing
        assertFalse( e.hasMoreElements() );
    }

    public void testEmptySearchPath()
        throws IOException
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader(), new URL[0] );
        final Enumeration<URL> e = space.findEntries( null, null, true );

        // local search should see nothing
        assertFalse( e.hasMoreElements() );
    }
}
