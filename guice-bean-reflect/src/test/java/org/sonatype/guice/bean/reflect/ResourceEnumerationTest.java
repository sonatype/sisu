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
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.codehaus.plexus.util.Expand;

public class ResourceEnumerationTest
    extends TestCase
{
    private static final URL COMMONS_LOGGING_JAR =
        ZipEntryIteratorTest.class.getClassLoader().getResource( "commons-logging-1.1.1.jar" );

    public void testResourceEnumeration()
        throws Exception
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( new URL[] { COMMONS_LOGGING_JAR, new URL( "http://127.0.0.1/" ),
                expand( COMMONS_LOGGING_JAR ) }, null, null, true );

        int n = 0;
        while ( e.hasMoreElements() )
        {
            e.nextElement();
            n++;
        }
        assertEquals( 84, n );

        try
        {
            e.nextElement();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException nse )
        {
        }
    }

    public void testStarredEnumeration()
    {
        int n = 0;
        final Enumeration<URL> e = new ResourceEnumeration( new URL[] { COMMONS_LOGGING_JAR }, "/", "*", true );
        while ( e.hasMoreElements() )
        {
            e.nextElement();
            n++;
        }
        assertEquals( 42, n );
    }

    public void testGlobbedEnumerationStart()
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( new URL[] { COMMONS_LOGGING_JAR }, null, "**$2.class", true );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$2.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testGlobbedEnumerationEnd()
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( new URL[] { COMMONS_LOGGING_JAR }, null, "SimpleLog.**", true );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/impl/SimpleLog.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testMultiGlobbedEnumeration()
    {
        final Enumeration<URL> e = new ResourceEnumeration( new URL[] { COMMONS_LOGGING_JAR }, null, "*Fact*$*", true );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$1.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$3.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$1.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$2.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$3.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$4.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$5.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$6.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testSubPathEnumeration()
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( new URL[] { COMMONS_LOGGING_JAR }, "/org/apache/commons/logging/impl", "*Fact*$*",
                                     true );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$1.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$3.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testNonRecursiveSubPathEnumeration()
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( new URL[] { COMMONS_LOGGING_JAR }, "/org/apache/commons/logging/", "*Fact*$*",
                                     false );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$1.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$2.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$3.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$4.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$5.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$6.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testBrokenUrlEnumeration()
        throws Exception
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( new URL[] { expand( COMMONS_LOGGING_JAR ) }, null, null, true );

        assertTrue( e.hasMoreElements() );

        // intentionally break the internal cached path to trigger a malformed URL problem
        final Field cachedEntry = ResourceEnumeration.class.getDeclaredField( "cachedEntry" );
        cachedEntry.setAccessible( true );
        cachedEntry.set( e, "foo:" );

        try
        {
            System.out.println( e.nextElement() );
            fail( "Expected IllegalStateException" );
        }
        catch ( final IllegalStateException ise )
        {
        }
    }

    private static URL expand( final URL url )
        throws Exception
    {
        final File jar = new File( url.toURI() );
        final File dir = new File( jar.getParentFile(), jar.getName() + "_expanded" );
        final Expand expander = new Expand();
        expander.setSrc( jar );
        expander.setDest( dir );
        dir.mkdirs();
        expander.execute();
        return dir.toURI().toURL();
    }
}
