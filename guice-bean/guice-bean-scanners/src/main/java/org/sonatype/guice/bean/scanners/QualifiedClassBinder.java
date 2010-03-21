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

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Qualifier;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.util.Jsr330;

/**
 * Auto-wires the qualified bean according to the attached {@link Qualifier} metadata.
 */
final class QualifiedClassBinder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

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
        // handle situations where qualifiers are normalized away
        final Class primaryInterface = getPrimaryInterface( clazz );
        final Annotation qualifier = normalizeQualifier( getQualifier( clazz ), clazz );
        if ( null != qualifier )
        {
            binder.bind( Key.get( primaryInterface, qualifier ) ).to( clazz );
        }
        else if ( primaryInterface != clazz )
        {
            binder.bind( Key.get( primaryInterface ) ).to( clazz );
        }
        else
        {
            binder.bind( clazz ); // implementation is the API
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Annotation normalizeQualifier( final Annotation qualifier, final Class<?> clazz )
    {
        if ( qualifier instanceof Named )
        {
            // Empty @Named needs auto-configuration
            final Named named = (Named) qualifier;
            if ( named.value().length() == 0 )
            {
                // @Named default classes don't need any qualifier
                if ( clazz.getSimpleName().startsWith( "Default" ) )
                {
                    return null;
                }
                // use FQN as the replacement qualifier
                return Jsr330.named( clazz.getName() );
            }
        }
        return qualifier; // no normalization required
    }

    private Annotation getQualifier( final Class<?> clazz )
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
    private Class getPrimaryInterface( final Class<?> clazz )
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
            final Class superInterface = getPrimaryInterface( superClazz );
            if ( superInterface != superClazz )
            {
                return superInterface;
            }
        }
        return clazz; // our implementation is the API
    }
}
