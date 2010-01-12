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
        assertEquals( clazzName, clazz.get().getName() );
        assertFalse( Dummy.class.equals( clazz.get() ) );
    }

    public void testMissingStrongDeferredClass()
    {
        try
        {
            final ClassSpace space = new URLClassSpace( testLoader );
            new StrongDeferredClass<Object>( space, "unknown-class" ).get();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
            System.out.println( e.toString() );
        }
    }
}
