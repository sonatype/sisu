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

final class QualifiedClassBinder
{
    private final Binder binder;

    QualifiedClassBinder( final Binder binder )
    {
        this.binder = binder;
    }

    @SuppressWarnings( "unchecked" )
    void bind( final Class<?> clazz )
    {
        Annotation qualifier = getQualifier( clazz );
        if ( qualifier instanceof Named )
        {
            final Named named = (Named) qualifier;
            if ( named.value().length() == 0 )
            {
                if ( clazz.getSimpleName().startsWith( "Default" ) )
                {
                    qualifier = null;
                }
                else
                {
                    qualifier = Jsr330.named( clazz.getName() );
                }
            }
        }

        final Key key =
            null == qualifier ? Key.get( getInterface( clazz ) ) : Key.get( getInterface( clazz ), qualifier );

        if ( !key.equals( Key.get( clazz ) ) )
        {
            binder.bind( key ).to( clazz );
        }
        else
        {
            binder.bind( key );
        }
    }

    private Annotation getQualifier( final Class<?> clazz )
    {
        for ( final Annotation a : clazz.getAnnotations() )
        {
            if ( a.annotationType().isAnnotationPresent( Qualifier.class ) )
            {
                return a;
            }
        }
        final Class<?> superClazz = clazz.getSuperclass();
        if ( null != superClazz )
        {
            return getQualifier( clazz.getSuperclass() );
        }
        return null;
    }

    private Class<?> getInterface( final Class<?> clazz )
    {
        final Typed typed = clazz.getAnnotation( Typed.class );
        if ( null != typed && typed.value().length > 0 )
        {
            return typed.value()[0];
        }
        final Class<?>[] interfaces = clazz.getInterfaces();
        if ( interfaces.length > 0 )
        {
            return interfaces[0];
        }
        final Class<?> superClazz = clazz.getSuperclass();
        if ( null != superClazz && superClazz.getSimpleName().startsWith( "Abstract" ) )
        {
            final Class<?> superInterface = getInterface( superClazz );
            if ( superInterface != superClazz )
            {
                return superInterface;
            }
        }
        return clazz;
    }
}
