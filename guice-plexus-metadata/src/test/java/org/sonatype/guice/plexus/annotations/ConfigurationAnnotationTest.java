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
package org.sonatype.guice.plexus.annotations;

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Configuration;

public class ConfigurationAnnotationTest
    extends TestCase
{
    @Configuration( "Default" )
    String defaultConfig;

    @Configuration( name = "Name", value = "Default" )
    String namedConfig;

    @Configuration( "${property}" )
    String propertyConfig;

    public void testConfigurationImpl()
        throws NoSuchFieldException
    {
        checkBehaviour( "defaultConfig" );
        checkBehaviour( "namedConfig" );
        checkBehaviour( "propertyConfig" );

        assertFalse( replicate( getConfiguration( "defaultConfig" ) ).equals( getConfiguration( "namedConfig" ) ) );
        assertFalse( replicate( getConfiguration( "defaultConfig" ) ).equals( getConfiguration( "propertyConfig" ) ) );
    }

    private static void checkBehaviour( final String name )
        throws NoSuchFieldException
    {
        final Configuration orig = getConfiguration( name );
        final Configuration clone = replicate( orig );

        assertTrue( orig.equals( clone ) );
        assertTrue( clone.equals( orig ) );
        assertTrue( clone.equals( clone ) );
        assertFalse( clone.equals( "" ) );

        assertEquals( orig.hashCode(), clone.hashCode() );

        assertEquals( new HashSet<String>( Arrays.asList( orig.toString().split( "[(, )]" ) ) ),
                      new HashSet<String>( Arrays.asList( clone.toString().split( "[(, )]" ) ) ) );

        assertEquals( orig.annotationType(), clone.annotationType() );
    }

    private static Configuration getConfiguration( final String name )
        throws NoSuchFieldException
    {
        return ConfigurationAnnotationTest.class.getDeclaredField( name ).getAnnotation( Configuration.class );
    }

    private static Configuration replicate( final Configuration orig )
    {
        return new ConfigurationImpl( orig.name(), orig.value() );
    }

    public void testNullChecks()
    {
        checkNullNotAllowed( null, "" );
        checkNullNotAllowed( "", null );
    }

    private static void checkNullNotAllowed( final String name, final String value )
    {
        try
        {
            new ConfigurationImpl( name, value );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
        }
    }
}
