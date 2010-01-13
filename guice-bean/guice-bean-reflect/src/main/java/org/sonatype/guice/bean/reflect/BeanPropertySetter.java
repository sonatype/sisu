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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.google.inject.TypeLiteral;

/**
 * {@link BeanProperty} backed by a single-parameter setter {@link Method}.
 */
final class BeanPropertySetter<T>
    implements BeanProperty<T>, PrivilegedAction<Void>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String BEAN_SETTER_ERROR = "Error calling bean setter: %s reason: %s";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Method method;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanPropertySetter( final Method method )
    {
        this.method = method;
    }

    // ----------------------------------------------------------------------
    // InjectableProperty methods
    // ----------------------------------------------------------------------

    public <A extends Annotation> A getAnnotation( final Class<A> annotationType )
    {
        return method.getAnnotation( annotationType );
    }

    @SuppressWarnings( "unchecked" )
    public TypeLiteral getType()
    {
        return TypeLiteral.get( method.getGenericParameterTypes()[0] );
    }

    public String getName()
    {
        final String name = method.getName();

        // this is guaranteed OK by the checks made in the BeanProperties code
        return Character.toLowerCase( name.charAt( 3 ) ) + name.substring( 4 );
    }

    public <B> void set( final B bean, final T value )
    {
        if ( !method.isAccessible() )
        {
            // ensure we can update the property
            AccessController.doPrivileged( this );
        }

        try
        {
            method.invoke( bean, value );
        }
        catch ( final InvocationTargetException e )
        {
            throw new RuntimeException( String.format( BEAN_SETTER_ERROR, method, e.getTargetException() ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( String.format( BEAN_SETTER_ERROR, method, e ) );
        }
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    public Void run()
    {
        // enable private injection
        method.setAccessible( true );
        return null;
    }
}
