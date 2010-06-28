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
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.google.inject.TypeLiteral;

/**
 * {@link BeanProperty} backed by a {@link Field}.
 */
final class BeanPropertyField<T>
    implements BeanProperty<T>, PrivilegedAction<Void>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String BEAN_FIELD_ERROR = "Error updating bean field: %s reason: %s";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Field field;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanPropertyField( final Field field )
    {
        this.field = field;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <A extends Annotation> A getAnnotation( final Class<A> annotationType )
    {
        return field.getAnnotation( annotationType );
    }

    @SuppressWarnings( "unchecked" )
    public TypeLiteral<T> getType()
    {
        return (TypeLiteral<T>) TypeLiteral.get( field.getGenericType() );
    }

    public String getName()
    {
        return field.getName();
    }

    public <B> void set( final B bean, final T value )
    {
        if ( !field.isAccessible() )
        {
            // make sure we can apply the binding
            AccessController.doPrivileged( this );
        }

        try
        {
            field.set( bean, value );
        }
        catch ( final Throwable e )
        {
            throw new RuntimeException( String.format( BEAN_FIELD_ERROR, field, e ) );
        }
    }

    @Override
    public int hashCode()
    {
        return field.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof BeanPropertyField<?> )
        {
            return field.equals( ( (BeanPropertyField<?>) rhs ).field );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return field.toString();
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    public Void run()
    {
        // enable private injection
        field.setAccessible( true );
        return null;
    }
}
