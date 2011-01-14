/**
 * Copyright (c) 2009-2011 Sonatype, Inc. All rights reserved.
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

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.inject.TypeLiteral;

public class TypeParametersTest
    extends TestCase
{
    static TypeLiteral<Object> OBJECT_TYPE = TypeLiteral.get( Object.class );

    static TypeLiteral<String> STRING_TYPE = TypeLiteral.get( String.class );

    static TypeLiteral<Float> FLOAT_TYPE = TypeLiteral.get( Float.class );

    static TypeLiteral<Short> SHORT_TYPE = TypeLiteral.get( Short.class );

    @SuppressWarnings( "rawtypes" )
    List rawList;

    List<Short> shortList;

    List<?> wildcardList;

    List<? extends String> wildcardStringList;

    @SuppressWarnings( "rawtypes" )
    Map rawMap;

    Map<String, Float> stringFloatMap;

    Map<?, ?> wildcardMap;

    Map<? extends Float, ? extends Short> wildcardFloatShortMap;

    public void testTypeArguments()
    {
        TypeLiteral<?>[] types;

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "rawList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "rawList" ) );
        assertEquals( 0, types.length );

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "rawMap" ), 0 ) );
        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "rawMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "rawMap" ) );
        assertEquals( 0, types.length );

        assertEquals( SHORT_TYPE, TypeParameters.get( getFieldType( "shortList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "shortList" ) );
        assertEquals( 1, types.length );
        assertEquals( SHORT_TYPE, types[0] );

        assertEquals( STRING_TYPE, TypeParameters.get( getFieldType( "stringFloatMap" ), 0 ) );
        assertEquals( FLOAT_TYPE, TypeParameters.get( getFieldType( "stringFloatMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "stringFloatMap" ) );
        assertEquals( 2, types.length );
        assertEquals( STRING_TYPE, types[0] );
        assertEquals( FLOAT_TYPE, types[1] );

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "wildcardList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "wildcardList" ) );
        assertEquals( 1, types.length );
        assertEquals( OBJECT_TYPE, types[0] );

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "wildcardMap" ), 0 ) );
        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "wildcardMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "wildcardMap" ) );
        assertEquals( 2, types.length );
        assertEquals( OBJECT_TYPE, types[0] );
        assertEquals( OBJECT_TYPE, types[1] );

        assertEquals( STRING_TYPE, TypeParameters.get( getFieldType( "wildcardStringList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "wildcardStringList" ) );
        assertEquals( 1, types.length );
        assertEquals( STRING_TYPE, types[0] );

        assertEquals( FLOAT_TYPE, TypeParameters.get( getFieldType( "wildcardFloatShortMap" ), 0 ) );
        assertEquals( SHORT_TYPE, TypeParameters.get( getFieldType( "wildcardFloatShortMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "wildcardFloatShortMap" ) );
        assertEquals( 2, types.length );
        assertEquals( FLOAT_TYPE, types[0] );
        assertEquals( SHORT_TYPE, types[1] );
    }

    @SuppressWarnings( "rawtypes" )
    List[] rawListArray;

    List<Short>[] shortListArray;

    List<?>[] wildcardListArray;

    List<? extends String>[] wildcardStringListArray;

    @SuppressWarnings( "rawtypes" )
    Map[] rawMapArray;

    Map<String, Float>[] stringFloatMapArray;

    Map<?, ?>[] wildcardMapArray;

    Map<? extends Float, ? extends Short>[] wildcardFloatShortMapArray;

    List<String[]> stringArrayList;

    public void testComponentType()
    {
        TypeLiteral<?>[] types;

        types = TypeParameters.get( getFieldType( "rawListArray" ) );
        assertEquals( getFieldType( "rawList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "rawListArray" ), 0 ) );

        types = TypeParameters.get( getFieldType( "rawMapArray" ) );
        assertEquals( getFieldType( "rawMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "rawMapArray" ), 0 ) );

        types = TypeParameters.get( getFieldType( "shortListArray" ) );
        assertEquals( getFieldType( "shortList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "shortListArray" ), 0 ) );

        types = TypeParameters.get( getFieldType( "stringFloatMapArray" ) );
        assertEquals( getFieldType( "stringFloatMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "stringFloatMapArray" ), 0 ) );

        types = TypeParameters.get( getFieldType( "wildcardListArray" ) );
        assertEquals( getFieldType( "wildcardList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardListArray" ), 0 ) );

        types = TypeParameters.get( getFieldType( "wildcardMapArray" ) );
        assertEquals( getFieldType( "wildcardMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardMapArray" ), 0 ) );

        types = TypeParameters.get( getFieldType( "wildcardStringListArray" ) );
        assertEquals( getFieldType( "wildcardStringList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardStringListArray" ), 0 ) );

        types = TypeParameters.get( getFieldType( "wildcardFloatShortMapArray" ) );
        assertEquals( getFieldType( "wildcardFloatShortMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardFloatShortMapArray" ), 0 ) );

        types = TypeParameters.get( TypeParameters.get( getFieldType( "stringArrayList" ) )[0] );
        assertEquals( STRING_TYPE, types[0] );
        assertEquals( types[0], TypeParameters.get( TypeParameters.get( getFieldType( "stringArrayList" ), 0 ), 0 ) );
    }

    public void testTypeParameterRangeChecks()
    {
        try
        {
            TypeParameters.get( getFieldType( "stringFloatMap" ), -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }
        try
        {
            TypeParameters.get( getFieldType( "stringFloatMap" ), 2 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }

        try
        {
            TypeParameters.get( getFieldType( "wildcardStringListArray" ), -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }
        try
        {
            TypeParameters.get( getFieldType( "wildcardStringListArray" ), 1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }
    }

    private static TypeLiteral<?> getFieldType( final String name )
    {
        try
        {
            return TypeLiteral.get( TypeParametersTest.class.getDeclaredField( name ).getGenericType() );
        }
        catch ( final NoSuchFieldException e )
        {
            throw new IllegalArgumentException( "Unknown test field " + name );
        }
    }
}
