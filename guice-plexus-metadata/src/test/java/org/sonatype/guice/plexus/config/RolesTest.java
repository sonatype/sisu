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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;

public class RolesTest
    extends TestCase
{
    private static final TypeLiteral<Object> OBJECT_LITERAL = TypeLiteral.get( Object.class );

    private static final TypeLiteral<String> STRING_LITERAL = TypeLiteral.get( String.class );

    private static final Key<Object> OBJECT_COMPONENT_KEY = Key.get( Object.class );

    private static final Key<Object> OBJECT_FOO_COMPONENT_KEY = Key.get( Object.class, Names.named( "foo" ) );

    public void testCanonicalRoleHint()
    {
        assertEquals( OBJECT_LITERAL + ":default", Roles.canonicalRoleHint( Object.class.getName(), null ) );
        assertEquals( OBJECT_LITERAL + ":default", Roles.canonicalRoleHint( Object.class.getName(), "" ) );
        assertEquals( OBJECT_LITERAL + ":default", Roles.canonicalRoleHint( Object.class.getName(), "default" ) );
        assertEquals( OBJECT_LITERAL + ":foo", Roles.canonicalRoleHint( Object.class.getName(), "foo" ) );
    }

    public void testDefaultComponentKeys()
    {
        assertEquals( OBJECT_COMPONENT_KEY, Roles.componentKey( Object.class, null ) );
        assertEquals( OBJECT_COMPONENT_KEY, Roles.componentKey( Object.class, "" ) );
        assertEquals( OBJECT_COMPONENT_KEY, Roles.componentKey( Object.class, "default" ) );
        assertEquals( OBJECT_COMPONENT_KEY, Roles.componentKey( component( "" ) ) );
        assertEquals( OBJECT_COMPONENT_KEY, Roles.componentKey( component( "default" ) ) );
    }

    public void testComponentKeys()
    {
        assertEquals( OBJECT_FOO_COMPONENT_KEY, Roles.componentKey( Object.class, "foo" ) );
        assertEquals( OBJECT_FOO_COMPONENT_KEY, Roles.componentKey( component( "foo" ) ) );
    }

    public void testRoleAnalysis()
    {
        assertEquals( STRING_LITERAL, Roles.roleType( requirement( String.class ), OBJECT_LITERAL ) );
        assertEquals( STRING_LITERAL, Roles.roleType( requirement( Object.class ), STRING_LITERAL ) );

        assertEquals( STRING_LITERAL, Roles.roleType( requirement( Object.class ),
                                                      TypeLiteral.get( Types.listOf( String.class ) ) ) );

        assertEquals( STRING_LITERAL, Roles.roleType( requirement( Object.class ),
                                                      TypeLiteral.get( Types.mapOf( Object.class, String.class ) ) ) );
    }

    private static Component component( final String hint )
    {
        return new ComponentImpl( Roles.defer( Object.class ), hint, "per-lookup" );
    }

    private static Requirement requirement( final Class<?> role )
    {
        return new RequirementImpl( Roles.defer( role ), true );
    }

    static class ClassicSpace
        implements ClassSpace
    {
        public Class<?> loadClass( final String name )
            throws ClassNotFoundException
        {
            return getClass().getClassLoader().loadClass( name );
        }

        public Enumeration<URL> getResources( final String name )
            throws IOException
        {
            return getClass().getClassLoader().getResources( name );
        }
    }

    static class BrokenSpace
        implements ClassSpace
    {
        public Class<?> loadClass( final String name )
            throws ClassNotFoundException
        {
            throw new ClassNotFoundException();
        }

        public Enumeration<URL> getResources( final String name )
            throws IOException
        {
            throw new IOException();
        }
    }

    public void testClassicDeferredClass()
    {
        final DeferredClass<?> clazz1 = Roles.defer( List.class );
        final DeferredClass<?> clazz2 = Roles.defer( new ClassicSpace(), "java.util.List" );

        assertEquals( List.class, clazz1.get() );
        assertEquals( List.class, clazz2.get() );
        assertEquals( clazz1.get(), clazz2.get() );

        assertEquals( "java.util.List", clazz1.getName() );
        assertEquals( "java.util.List", clazz2.getName() );
    }

    public void testBrokenDeferredClass()
    {
        final DeferredClass<?> clazz = Roles.defer( new BrokenSpace(), "java.util.List" );

        try
        {
            clazz.get();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }

        assertEquals( "java.util.List", clazz.getName() );
    }
}
