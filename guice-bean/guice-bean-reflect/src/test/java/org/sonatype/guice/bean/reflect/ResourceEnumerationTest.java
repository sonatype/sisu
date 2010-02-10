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

import static org.sonatype.guice.bean.reflect.FileEntryIteratorTest.expand;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class ResourceEnumerationTest
    extends TestCase
{
    private static final URL COMMONS_LOGGING_JAR =
        ZipEntryIteratorTest.class.getClassLoader().getResource( "commons-logging-1.1.1.jar" );

    public void testResourceEnumeration()
        throws Exception
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( null, null, true, new URL[] { COMMONS_LOGGING_JAR, expand( COMMONS_LOGGING_JAR ) } );

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
        final Enumeration<URL> e = new ResourceEnumeration( "/", "*", true, new URL[] { COMMONS_LOGGING_JAR } );
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
            new ResourceEnumeration( null, "**$2.class", true, new URL[] { COMMONS_LOGGING_JAR } );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/LogFactory$2.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testGlobbedEnumerationEnd()
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( null, "SimpleLog.**", true, new URL[] { COMMONS_LOGGING_JAR } );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/impl/SimpleLog.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testMultiGlobbedEnumeration()
    {
        final Enumeration<URL> e = new ResourceEnumeration( null, "*Fact*$*", true, new URL[] { COMMONS_LOGGING_JAR } );

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
            new ResourceEnumeration( "/org/apache/commons/logging/impl", "*Fact*$*", true,
                                     new URL[] { COMMONS_LOGGING_JAR } );

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$1.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class", e.nextElement().getPath() );
        assertEquals( prefix + "org/apache/commons/logging/impl/LogFactoryImpl$3.class", e.nextElement().getPath() );
        assertFalse( e.hasMoreElements() );
    }

    public void testNonRecursiveSubPathEnumeration()
    {
        final Enumeration<URL> e =
            new ResourceEnumeration( "/org/apache/commons/logging/", "*Fact*$*", false,
                                     new URL[] { COMMONS_LOGGING_JAR } );

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
            new ResourceEnumeration( null, null, true, new URL[] { expand( COMMONS_LOGGING_JAR ) } );

        assertTrue( e.hasMoreElements() );

        // intentionally break the internal cached path to trigger a malformed URL problem
        final Field nextEntry = ResourceEnumeration.class.getDeclaredField( "nextEntry" );
        nextEntry.setAccessible( true );
        nextEntry.set( e, "foo:" );

        try
        {
            e.nextElement();
            fail( "Expected IllegalStateException" );
        }
        catch ( final IllegalStateException ise )
        {
        }
    }
}
