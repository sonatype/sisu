/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

/**
 * {@link BeanProperty} backed by a {@link Field}.
 */
final class BeanPropertyField<T>
    implements BeanProperty<T>, PrivilegedAction<Void>
{
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
        catch ( final Exception e )
        {
            throw new ProvisionException( "Error injecting: " + field, e );
        }
        catch ( final LinkageError e )
        {
            throw new ProvisionException( "Error injecting: " + field, e );
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
