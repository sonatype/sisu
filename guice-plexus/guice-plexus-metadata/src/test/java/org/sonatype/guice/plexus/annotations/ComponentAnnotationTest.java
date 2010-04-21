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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.plexus.config.Strategies;

public class ComponentAnnotationTest
    extends TestCase
{
    interface A
    {
    }

    @Component( role = A.class )
    static class DefaultA
        implements A
    {
    }

    @Component( role = A.class, hint = "Named" )
    static class NamedA
        implements A
    {
    }

    @Component( role = A.class, description = "Something" )
    static class DescribedA
        implements A
    {
    }

    @Component( role = A.class, instantiationStrategy = Strategies.PER_LOOKUP )
    static class PrototypeA
        implements A
    {
    }

    @Component( role = A.class, hint = "Named", instantiationStrategy = Strategies.PER_LOOKUP )
    static class NamedPrototypeA
        implements A
    {
    }

    @Component( role = Simple.class )
    static class Simple
    {
    }

    @Component( role = Simple.class, version = "2" )
    static class Simple2
        extends Simple
    {
    }

    @Component( role = Simple.class, isolatedRealm = true )
    static class Simple3
        extends Simple
    {
    }

    public void testComponentImpl()
        throws ClassNotFoundException
    {
        checkBehaviour( "DefaultA" );
        checkBehaviour( "NamedA" );
        checkBehaviour( "PrototypeA" );
        checkBehaviour( "NamedPrototypeA" );
        checkBehaviour( "DescribedA" );

        assertFalse( replicate( getComponent( "DefaultA" ) ).equals( getComponent( "NamedA" ) ) );
        assertFalse( replicate( getComponent( "DefaultA" ) ).equals( getComponent( "PrototypeA" ) ) );
        assertFalse( replicate( getComponent( "DefaultA" ) ).equals( getComponent( "DescribedA" ) ) );
        assertFalse( replicate( getComponent( "Simple" ) ).equals( getComponent( "DefaultA" ) ) );
        assertFalse( replicate( getComponent( "Simple" ) ).equals( getComponent( "Simple2" ) ) );
        assertFalse( replicate( getComponent( "Simple" ) ).equals( getComponent( "Simple3" ) ) );
    }

    private static void checkBehaviour( final String name )
        throws ClassNotFoundException
    {
        final Component orig = getComponent( name );
        final Component clone = replicate( orig );

        assertTrue( orig.equals( clone ) );
        assertTrue( clone.equals( orig ) );
        assertTrue( clone.equals( clone ) );
        assertFalse( clone.equals( "" ) );

        assertEquals( orig.hashCode(), clone.hashCode() );

        assertEquals( new HashSet<String>( Arrays.asList( orig.toString().split( "[(, )]" ) ) ),
                      new HashSet<String>( Arrays.asList( clone.toString().split( "[(, )]" ) ) ) );

        assertEquals( orig.annotationType(), clone.annotationType() );
    }

    private static Component getComponent( final String name )
        throws ClassNotFoundException
    {
        return Class.forName( ComponentAnnotationTest.class.getName() + '$' + name ).getAnnotation( Component.class );
    }

    private static Component replicate( final Component orig )
    {
        return new ComponentImpl( orig.role(), orig.hint(), orig.instantiationStrategy(), orig.description() );
    }

    public void testNullChecks()
    {
        checkNullNotAllowed( null, "", "", "" );
        checkNullNotAllowed( Object.class, null, "", "" );
        checkNullNotAllowed( Object.class, "", null, "" );
        checkNullNotAllowed( Object.class, "", "", null );
    }

    private static void checkNullNotAllowed( final Class<?> role, final String hint, final String instantationStrategy,
                                             final String description )
    {
        try
        {
            new ComponentImpl( role, hint, instantationStrategy, description );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
        }
    }
}
