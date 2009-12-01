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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import com.google.inject.TypeLiteral;

/**
 * Constants and utility methods for dealing with reified generic types.
 */
public final class Generics
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final TypeLiteral<Object> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Generics()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new Generics(); // keep Cobertura coverage happy
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Extracts a type argument from a generic type, for example {@code Bar} from {@code List<Bar>}.
     * 
     * @param genericType The generic type
     * @param index The type argument index
     * @return Selected type argument
     */
    public static TypeLiteral<?> typeArgument( final TypeLiteral<?> genericType, final int index )
    {
        final Type type = genericType.getType();
        if ( type instanceof ParameterizedType )
        {
            return expandType( ( (ParameterizedType) type ).getActualTypeArguments()[index] );
        }
        return OBJECT_TYPE_LITERAL;
    }

    /**
     * Extracts the component type from an array type, for example {@code Bar} from {@code Bar[]}.
     * 
     * @param arrayType The array type
     * @return Component type of the array
     */
    public static TypeLiteral<?> componentType( final TypeLiteral<?> arrayType )
    {
        final Type type = arrayType.getType();
        if ( type instanceof GenericArrayType )
        {
            return expandType( ( (GenericArrayType) type ).getGenericComponentType() );
        }
        return TypeLiteral.get( arrayType.getRawType().getComponentType() );
    }

    /**
     * Expands wild-card types where possible, for example {@code Bar} from {@code ? extends Bar}.
     * 
     * @param type The generic type
     * @return Widened type that is still assignment-compatible with the original.
     */
    private static TypeLiteral<?> expandType( final Type type )
    {
        return TypeLiteral.get( type instanceof WildcardType ? ( (WildcardType) type ).getUpperBounds()[0] : type );
    }
}
