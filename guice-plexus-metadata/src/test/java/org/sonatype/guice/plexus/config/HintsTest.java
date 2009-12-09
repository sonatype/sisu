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
package org.sonatype.guice.plexus.config;

import java.util.Arrays;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.StrongClassSpace;
import org.sonatype.guice.plexus.annotations.RequirementImpl;

public class HintsTest
    extends TestCase
{
    public void testCanonicalHint()
    {
        assertEquals( "default", Hints.canonicalHint( null ) );
        assertEquals( "default", Hints.canonicalHint( "" ) );
        assertEquals( "default", Hints.canonicalHint( new String( "default" ) ) );
        assertEquals( "foo", Hints.canonicalHint( "foo" ) );
    }

    public void testCanonicalHints()
    {
        assertArrayEquals( new String[0], Hints.canonicalHints() );
        assertArrayEquals( new String[0], Hints.canonicalHints( requirement() ) );
        assertArrayEquals( new String[0], Hints.canonicalHints( requirement( "" ) ) );
        assertArrayEquals( new String[] { "default" }, Hints.canonicalHints( requirement( "default" ) ) );
        assertArrayEquals( new String[] { "foo" }, Hints.canonicalHints( requirement( "foo" ) ) );
        assertArrayEquals( new String[] { "default", "foo" }, Hints.canonicalHints( requirement( "", "foo" ) ) );
        assertArrayEquals( new String[] { "foo", "default" }, Hints.canonicalHints( requirement( "foo", "" ) ) );
    }

    public void testHintsAreInterned()
    {
        assertSame( "hint", Hints.canonicalHint( new String( "hint" ) ) );
        assertSame( "hint", Hints.canonicalHints( requirement( new String( "hint" ) ) )[0] );
        final Requirement requirement = requirement( new String( "foo" ), new String( "bar" ) );
        assertSame( "foo", Hints.canonicalHints( requirement )[0] );
        assertSame( "bar", Hints.canonicalHints( requirement )[1] );
        assertNotSame( new String( "hint" ), Hints.canonicalHint( "hint" ) );
        assertEquals( new String( "hint" ), Hints.canonicalHint( "hint" ) );
    }

    public void testIsDefaultHint()
    {
        assertTrue( Hints.isDefaultHint( null ) );
        assertTrue( Hints.isDefaultHint( "" ) );
        assertTrue( Hints.isDefaultHint( new String( "default" ) ) );
        assertFalse( Hints.isDefaultHint( "foo" ) );
    }

    private static <T> void assertArrayEquals( final T[] a, final T[] b )
    {
        assertTrue( "Expected: " + Arrays.toString( a ) + "but was: " + Arrays.toString( b ), Arrays.equals( a, b ) );
    }

    private static Requirement requirement( final String... hints )
    {
        return new RequirementImpl( defer( Object.class ), true, hints );
    }

    private static DeferredClass<?> defer( final Class<?> clazz )
    {
        return new StrongClassSpace( TestCase.class.getClassLoader() ).deferLoadClass( clazz.getName() );
    }
}
