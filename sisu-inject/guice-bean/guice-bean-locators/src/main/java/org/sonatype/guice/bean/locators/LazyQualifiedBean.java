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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.sonatype.inject.Description;

import com.google.inject.Binding;
import com.google.inject.Scopes;

/**
 * Lazy {@link QualifiedBean} backed by a qualified {@link Binding}.
 */
final class LazyQualifiedBean<Q extends Annotation, T>
    implements QualifiedBean<Q, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Q qualifier;

    private final Binding<T> binding;

    private final Provider<T> provider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyQualifiedBean( final Q qualifier, final Binding<T> binding )
    {
        this.qualifier = qualifier;
        this.binding = binding;

        if ( Scopes.isSingleton( binding ) )
        {
            this.provider = binding.getProvider();
        }
        else
        {
            // use Guice's singleton logic to get lazy-loading without introducing extra locks
            this.provider = Scopes.SINGLETON.scope( binding.getKey(), binding.getProvider() );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Q getKey()
    {
        return qualifier;
    }

    public T getValue()
    {
        return provider.get();
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public T get()
    {
        return getValue();
    }

    public String getDescription()
    {
        final Object source = binding.getSource();
        if ( source instanceof BeanDescription )
        {
            return ( (BeanDescription) source ).getDescription();
        }
        final Class<T> clazz = getImplementationClass();
        if ( null != clazz )
        {
            final Description description = clazz.getAnnotation( Description.class );
            if ( null != description )
            {
                return description.value();
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public Class<T> getImplementationClass()
    {
        return (Class<T>) binding.acceptTargetVisitor( ImplementationVisitor.THIS );
    }

    public Binding<? extends T> getBinding()
    {
        return binding;
    }

    @Override
    public String toString()
    {
        return getKey() + "=" + getValue();
    }
}
