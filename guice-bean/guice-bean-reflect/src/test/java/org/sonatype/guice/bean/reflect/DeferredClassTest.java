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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

public class DeferredClassTest
    extends TestCase
{
    URLClassLoader testLoader;

    @Override
    protected void setUp()
        throws MalformedURLException
    {
        testLoader = URLClassLoader.newInstance( new URL[] { new File( "target/test-classes" ).toURL() }, null );
    }

    private static class Dummy
    {
    }

    public void testStrongDeferredClass()
    {
        final String clazzName = Dummy.class.getName();
        final ClassSpace space = new URLClassSpace( testLoader );
        final DeferredClass<?> clazz = space.deferLoadClass( clazzName );

        assertEquals( clazzName, clazz.getName() );
        assertEquals( clazzName, clazz.load().getName() );
        assertFalse( Dummy.class.equals( clazz.load() ) );

        assertEquals( ( 17 * 31 + clazzName.hashCode() ) * 31 + space.hashCode(), clazz.hashCode() );

        assertEquals( clazz, clazz );

        assertEquals( new StrongDeferredClass<Object>( space, clazzName ), clazz );

        assertFalse( clazz.equals( new DeferredClass<Object>()
        {
            @SuppressWarnings( "unchecked" )
            public Class<Object> load()
                throws TypeNotPresentException
            {
                return (Class) clazz.load();
            }

            public String getName()
            {
                return clazz.getName();
            }

            public DeferredProvider<Object> asProvider()
            {
                throw new UnsupportedOperationException();
            }
        } ) );

        final String clazzName2 = clazzName + "$1";
        final ClassSpace space2 = new URLClassSpace( ClassLoader.getSystemClassLoader(), null );

        assertFalse( clazz.equals( new StrongDeferredClass<Object>( space, clazzName2 ) ) );
        assertFalse( clazz.equals( new StrongDeferredClass<Object>( space2, clazzName ) ) );

        assertTrue( clazz.toString().contains( clazzName ) );
        assertTrue( clazz.toString().contains( space.toString() ) );
    }

    public void testLoadedClass()
    {
        final DeferredClass<?> objectClazz = new LoadedClass<Object>( Object.class );
        final DeferredClass<?> stringClazz = new LoadedClass<String>( String.class );

        assertEquals( String.class.getName(), stringClazz.getName() );
        assertEquals( String.class.getName(), stringClazz.load().getName() );
        assertSame( String.class, stringClazz.load() );

        assertEquals( stringClazz, stringClazz );

        assertFalse( stringClazz.equals( objectClazz ) );
        assertFalse( stringClazz.equals( String.class ) );

        assertEquals( String.class.hashCode(), stringClazz.hashCode() );
        assertEquals( "Loaded " + String.class.toString(), stringClazz.toString() );
    }

    public void testMissingStrongDeferredClass()
    {
        try
        {
            final ClassSpace space = new URLClassSpace( testLoader );
            new StrongDeferredClass<Object>( space, "unknown-class" ).load();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }
    }
}
