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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import junit.framework.TestCase;

public class DeferredClassTest
    extends TestCase
{
    private static class Dummy
    {
    }

    public void testStrongDeferredClass()
        throws IOException
    {
        ClassLoader loader = new URLClassLoader( new URL[] { new File( "target/test-classes" ).toURL() }, null );

        final String clazzName = Dummy.class.getName();
        final ClassSpace space = new StrongClassSpace( loader );
        final DeferredClass<?> clazz = space.deferLoadClass( clazzName );

        assertEquals( clazzName, clazz.get().getName() );
        assertFalse( Dummy.class.equals( clazz.get() ) );

        final String resourceName = clazzName.replace( '.', '/' ) + ".class";
        Enumeration<URL> resources = space.getResources( resourceName );
        assertEquals( resourceName, resources.nextElement().getPath().replaceFirst( ".*/test-classes/", "" ) );
        assertFalse( resources.hasMoreElements() );

        // clear refs
        resources = null;
        loader = null;

        System.gc();
        new StringBuilder( 1024 );
        System.gc();
        new StringBuilder( 1024 );
        System.gc();
        new StringBuilder( 1024 );
        System.gc();
        new StringBuilder( 1024 );
        System.gc();

        // still there
        clazz.get();

        // still there
        space.getResources( resourceName );

        assertEquals( clazzName, clazz.getName() );
    }

    public void testMissingStrongDeferredClass()
    {
        try
        {
            final ClassSpace space = new StrongClassSpace( getClass().getClassLoader() );
            new StrongDeferredClass<Object>( space, "unknown-class" ).get();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }
    }

    public void testWeakDeferredClass()
        throws IOException
    {
        ClassLoader loader = new URLClassLoader( new URL[] { new File( "target/test-classes" ).toURL() }, null );

        final String clazzName = Dummy.class.getName();
        final ClassSpace space = new WeakClassSpace( loader );
        final DeferredClass<?> clazz = space.deferLoadClass( clazzName );

        assertEquals( clazzName, clazz.get().getName() );
        assertFalse( Dummy.class.equals( clazz.get() ) );

        final String resourceName = clazzName.replace( '.', '/' ) + ".class";
        Enumeration<URL> resources = space.getResources( resourceName );
        assertEquals( resourceName, resources.nextElement().getPath().replaceFirst( ".*/test-classes/", "" ) );
        assertFalse( resources.hasMoreElements() );

        // clear refs
        resources = null;
        loader = null;

        System.gc();
        new StringBuilder( 1024 );
        System.gc();
        new StringBuilder( 1024 );
        System.gc();
        new StringBuilder( 1024 );
        System.gc();
        new StringBuilder( 1024 );
        System.gc();

        try
        {
            // now gone
            clazz.get();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
            assertEquals( "ClassSpace has been unloaded.", e.getCause().getMessage() );
        }

        try
        {
            // now gone
            space.getResources( resourceName );
            fail( "Expected IOException" );
        }
        catch ( final IOException e )
        {
            assertEquals( "ClassSpace has been unloaded.", e.getMessage() );
        }

        assertEquals( clazzName, clazz.getName() );
    }

    public void testMissingWeakDeferredClass()
    {
        try
        {
            final ClassSpace space = new WeakClassSpace( getClass().getClassLoader() );
            new WeakDeferredClass<Object>( space, "unknown-class" ).get();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }
    }
}
