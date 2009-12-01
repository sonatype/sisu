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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.inject.TypeLiteral;

public class GenericsTest
    extends TestCase
{
    static TypeLiteral<Object> OBJECT_TYPE = TypeLiteral.get( Object.class );

    static TypeLiteral<String> STRING_TYPE = TypeLiteral.get( String.class );

    static TypeLiteral<Float> FLOAT_TYPE = TypeLiteral.get( Float.class );

    static TypeLiteral<Short> SHORT_TYPE = TypeLiteral.get( Short.class );

    @SuppressWarnings( "unchecked" )
    List rawList;

    List<Short> shortList;

    List<?> wildcardList;

    List<? extends String> wildcardStringList;

    @SuppressWarnings( "unchecked" )
    Map rawMap;

    Map<String, Float> stringFloatMap;

    Map<?, ?> wildcardMap;

    Map<? extends Float, ? extends Short> wildcardFloatShortMap;

    public void testTypeArguments()
    {
        assertEquals( OBJECT_TYPE, Generics.typeArgument( getFieldType( "rawList" ), 0 ) );
        assertEquals( OBJECT_TYPE, Generics.typeArgument( getFieldType( "rawMap" ), 0 ) );
        assertEquals( OBJECT_TYPE, Generics.typeArgument( getFieldType( "rawMap" ), 1 ) );

        assertEquals( SHORT_TYPE, Generics.typeArgument( getFieldType( "shortList" ), 0 ) );
        assertEquals( STRING_TYPE, Generics.typeArgument( getFieldType( "stringFloatMap" ), 0 ) );
        assertEquals( FLOAT_TYPE, Generics.typeArgument( getFieldType( "stringFloatMap" ), 1 ) );

        assertEquals( OBJECT_TYPE, Generics.typeArgument( getFieldType( "wildcardList" ), 0 ) );
        assertEquals( OBJECT_TYPE, Generics.typeArgument( getFieldType( "wildcardMap" ), 0 ) );
        assertEquals( OBJECT_TYPE, Generics.typeArgument( getFieldType( "wildcardMap" ), 1 ) );

        assertEquals( STRING_TYPE, Generics.typeArgument( getFieldType( "wildcardStringList" ), 0 ) );
        assertEquals( FLOAT_TYPE, Generics.typeArgument( getFieldType( "wildcardFloatShortMap" ), 0 ) );
        assertEquals( SHORT_TYPE, Generics.typeArgument( getFieldType( "wildcardFloatShortMap" ), 1 ) );
    }

    @SuppressWarnings( "unchecked" )
    List[] rawListArray;

    List<Short>[] shortListArray;

    List<?>[] wildcardListArray;

    List<? extends String>[] wildcardStringListArray;

    @SuppressWarnings( "unchecked" )
    Map[] rawMapArray;

    Map<String, Float>[] stringFloatMapArray;

    Map<?, ?>[] wildcardMapArray;

    Map<? extends Float, ? extends Short>[] wildcardFloatShortMapArray;

    List<String[]> stringArrayList;

    public void testComponentType()
    {
        assertEquals( getFieldType( "rawList" ), Generics.componentType( getFieldType( "rawListArray" ) ) );
        assertEquals( getFieldType( "rawMap" ), Generics.componentType( getFieldType( "rawMapArray" ) ) );

        assertEquals( getFieldType( "shortList" ), Generics.componentType( getFieldType( "shortListArray" ) ) );
        assertEquals( getFieldType( "stringFloatMap" ), Generics.componentType( getFieldType( "stringFloatMapArray" ) ) );

        assertEquals( getFieldType( "wildcardList" ), Generics.componentType( getFieldType( "wildcardListArray" ) ) );
        assertEquals( getFieldType( "wildcardMap" ), Generics.componentType( getFieldType( "wildcardMapArray" ) ) );

        assertEquals( getFieldType( "wildcardStringList" ),
                      Generics.componentType( getFieldType( "wildcardStringListArray" ) ) );
        assertEquals( getFieldType( "wildcardFloatShortMap" ),
                      Generics.componentType( getFieldType( "wildcardFloatShortMapArray" ) ) );

        assertEquals( STRING_TYPE,
                      Generics.componentType( Generics.typeArgument( getFieldType( "stringArrayList" ), 0 ) ) );
    }

    public void testCornerCase()
    {
        try
        {
            // Guice currently canonicalizes raw array types to generic array types.
            // But this might not always be the case, so force a raw type test here.
            final TypeLiteral<?> literal = TypeLiteral.get( String[].class );
            final Field typeField = TypeLiteral.class.getDeclaredField( "type" );
            typeField.setAccessible( true );
            typeField.set( literal, String[].class );

            assertEquals( STRING_TYPE, Generics.componentType( literal ) );
        }
        catch ( final NoSuchFieldException e )
        {
            fail( "Test assumes TypeLiteral has a field named \"type\"" );
        }
        catch ( final IllegalAccessException e )
        {
            fail( "Test wasn't able to update \"type\" field in TypeLiteral" );
        }
    }

    private static TypeLiteral<?> getFieldType( final String name )
    {
        try
        {
            return TypeLiteral.get( GenericsTest.class.getDeclaredField( name ).getGenericType() );
        }
        catch ( final NoSuchFieldException e )
        {
            throw new IllegalArgumentException( "Unknown test field " + name );
        }
    }
}
