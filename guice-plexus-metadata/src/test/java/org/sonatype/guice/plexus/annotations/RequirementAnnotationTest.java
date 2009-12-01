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
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.plexus.config.Roles;

public class RequirementAnnotationTest
    extends TestCase
{
    @Requirement
    String defaultReq;

    @Requirement( role = String.class )
    String stringReq;

    @Requirement( hint = "named" )
    String namedReq;

    @Requirement( optional = true )
    String optionalReq;

    @Requirement( hints = { "A", "B", "C" } )
    List<?> namedListReq;

    @Requirement( role = String.class, hint = "named" )
    String namedStringReq;

    @Requirement( role = String.class, hints = { "A", "B", "C" } )
    List<String> namedStringListReq;

    public void testRequirementImpl()
        throws NoSuchFieldException
    {
        checkBehaviour( "defaultReq" );
        checkBehaviour( "stringReq" );
        checkBehaviour( "namedReq" );
        checkBehaviour( "optionalReq" );
        checkBehaviour( "namedListReq" );
        checkBehaviour( "namedStringReq" );
        checkBehaviour( "namedStringListReq" );

        assertFalse( replicate( getRequirement( "defaultReq" ) ).equals( getRequirement( "stringReq" ) ) );
        assertFalse( replicate( getRequirement( "stringReq" ) ).equals( getRequirement( "namedStringReq" ) ) );
        assertFalse( replicate( getRequirement( "defaultReq" ) ).equals( getRequirement( "namedListReq" ) ) );
        assertFalse( replicate( getRequirement( "defaultReq" ) ).equals( getRequirement( "optionalReq" ) ) );
    }

    private static void checkBehaviour( final String name )
        throws NoSuchFieldException
    {
        final Requirement orig = getRequirement( name );
        final Requirement clone = replicate( orig );

        assertTrue( orig.equals( clone ) );
        assertTrue( clone.equals( orig ) );
        assertTrue( clone.equals( clone ) );
        assertFalse( clone.equals( "" ) );

        assertEquals( orig.hashCode(), clone.hashCode() );

        assertEquals( new HashSet<String>( Arrays.asList( orig.toString().split( "[(, )]" ) ) ),
                      new HashSet<String>( Arrays.asList( clone.toString().split( "[(, )]" ) ) ) );

        assertEquals( orig.annotationType(), clone.annotationType() );
    }

    private static Requirement getRequirement( final String name )
        throws NoSuchFieldException
    {
        return RequirementAnnotationTest.class.getDeclaredField( name ).getAnnotation( Requirement.class );
    }

    private static Requirement replicate( final Requirement orig )
    {
        final String h = orig.hint();

        return new RequirementImpl( Roles.defer( orig.role() ), orig.optional(), h.length() > 0 ? new String[] { h }
                        : orig.hints() );
    }

    public void testNullChecks()
    {
        checkNullNotAllowed( null );
        checkNullNotAllowed( Object.class, (String[]) null );
        checkNullNotAllowed( Object.class, new String[] { null } );
    }

    private static void checkNullNotAllowed( final Class<?> role, final String... hints )
    {
        try
        {
            new RequirementImpl( role != null ? Roles.defer( role ) : null, false, hints );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
        }
    }
}
