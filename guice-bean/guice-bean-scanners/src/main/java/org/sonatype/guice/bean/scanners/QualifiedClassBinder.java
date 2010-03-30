/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.scanners;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.sonatype.guice.bean.reflect.DeclaredMembers;
import org.sonatype.guice.bean.reflect.Generics;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Jsr330;
import com.google.inject.util.Types;

/**
 * Auto-wires the qualified bean according to the attached {@link Qualifier} metadata.
 */
final class QualifiedClassBinder
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Named DEFAULT_NAMED = Jsr330.named( "default" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<TypeLiteral<?>> boundTypes = new HashSet<TypeLiteral<?>>();

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedClassBinder( final Binder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    void bind( final Class clazz )
    {
        final Class keyType = getKeyType( clazz );
        final Annotation qualifier = normalizeQualifier( getQualifier( clazz ), clazz );
        if ( null != qualifier )
        {
            binder.bind( Key.get( keyType, qualifier ) ).to( clazz );
        }
        else if ( keyType != clazz )
        {
            binder.bind( Key.get( keyType ) ).to( clazz );
        }
        else
        {
            binder.bind( clazz ); // implementation is the API
        }
        bindQualifiedCollections( clazz );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static Annotation normalizeQualifier( final Annotation qualifier, final Class<?> clazz )
    {
        if ( qualifier instanceof Named )
        {
            final Named named = (Named) qualifier;
            final String hint = named.value();
            if ( hint.length() == 0 )
            {
                if ( clazz.getSimpleName().startsWith( "Default" ) )
                {
                    return DEFAULT_NAMED;
                }
                return Jsr330.named( clazz.getName() );
            }
            else if ( "default".equalsIgnoreCase( hint ) )
            {
                return DEFAULT_NAMED;
            }
        }
        return qualifier;
    }

    private static Annotation getQualifier( final Class<?> clazz )
    {
        for ( final Annotation ann : clazz.getAnnotations() )
        {
            // pick first annotation marked with a @Qualifier meta-annotation
            if ( ann.annotationType().isAnnotationPresent( Qualifier.class ) )
            {
                return ann;
            }
        }
        // must be somewhere in the class hierarchy
        return getQualifier( clazz.getSuperclass() );
    }

    @SuppressWarnings( "unchecked" )
    private static Class getKeyType( final Class<?> clazz )
    {
        // @Typed settings take precedence
        final Typed typed = clazz.getAnnotation( Typed.class );
        if ( null != typed && typed.value().length > 0 )
        {
            return typed.value()[0];
        }
        // followed by explicit declarations
        final Class[] interfaces = clazz.getInterfaces();
        if ( interfaces.length > 0 )
        {
            return interfaces[0];
        }
        // otherwise check the local superclass hierarchy
        final Class superClazz = clazz.getSuperclass();
        if ( !superClazz.getName().startsWith( "java" ) )
        {
            final Class superInterface = getKeyType( superClazz );
            if ( superInterface != superClazz )
            {
                return superInterface;
            }
        }
        return superClazz != Object.class ? superClazz : clazz;
    }

    private void bindQualifiedCollections( final Class<?> clazz )
    {
        for ( final Member member : new DeclaredMembers( clazz ) )
        {
            if ( member instanceof Field && ( (AnnotatedElement) member ).isAnnotationPresent( Inject.class ) )
            {
                final TypeLiteral<?> fieldType = TypeLiteral.get( ( (Field) member ).getGenericType() );
                if ( !boundTypes.add( fieldType ) )
                {
                    continue;
                }
                if ( fieldType.getRawType() == List.class )
                {
                    bindQualifiedList( fieldType );
                }
                else if ( fieldType.getRawType() == Map.class )
                {
                    bindQualifiedMap( fieldType );
                }
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    private void bindQualifiedList( final TypeLiteral type )
    {
        final Type beanType = Generics.typeArgument( type, 0 ).getType();
        final Type providerType = Types.newParameterizedType( ListProvider.class, beanType );
        binder.bind( type ).toProvider( Key.get( providerType ) );
    }

    @SuppressWarnings( "unchecked" )
    private void bindQualifiedMap( final TypeLiteral type )
    {
        final Type qualifierType = Generics.typeArgument( type, 0 ).getType();
        final Type beanType = Generics.typeArgument( type, 1 ).getType();
        final Type providerType;
        if ( qualifierType == String.class )
        {
            providerType = Types.newParameterizedType( MapHintProvider.class, beanType );
        }
        else
        {
            providerType = Types.newParameterizedType( MapProvider.class, qualifierType, beanType );
        }
        binder.bind( type ).toProvider( Key.get( providerType ) );
    }
}
