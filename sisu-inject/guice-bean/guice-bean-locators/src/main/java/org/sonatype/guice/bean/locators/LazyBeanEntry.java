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

import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Description;

import com.google.inject.Binding;
import com.google.inject.Scopes;

/**
 * Lazy {@link BeanEntry} backed by a qualified {@link Binding} and an assigned rank.
 */
final class LazyBeanEntry<Q extends Annotation, T>
    implements BeanEntry<Q, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Q qualifier;

    private final Binding<T> binding;

    private final Provider<T> provider;

    private final int rank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyBeanEntry( final Q qualifier, final Binding<T> binding, final int rank )
    {
        this.qualifier = qualifier;
        this.binding = binding;
        this.rank = rank;

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

    @SuppressWarnings( "unchecked" )
    public Q getKey()
    {
        if ( qualifier instanceof com.google.inject.name.Named )
        {
            return (Q) new JsrNamed( (com.google.inject.name.Named) qualifier );
        }
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

    public Object getSource()
    {
        return binding.getSource();
    }

    public int getRank()
    {
        return rank;
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder().append( getKey() ).append( '=' );
        try
        {
            buf.append( getValue() );
        }
        catch ( final Throwable e )
        {
            buf.append( e );
        }
        return buf.toString();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Implementation of @{@link javax.inject.Named} that can also act like @{@link com.google.inject.name.Named}.
     */
    private static final class JsrNamed
        implements com.google.inject.name.Named, javax.inject.Named
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final String value;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        JsrNamed( final com.google.inject.name.Named named )
        {
            value = named.value();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public String value()
        {
            return value;
        }

        public Class<? extends Annotation> annotationType()
        {
            return javax.inject.Named.class;
        }

        @Override
        public int hashCode()
        {
            return 127 * "value".hashCode() ^ value.hashCode();
        }

        @Override
        public boolean equals( final Object rhs )
        {
            if ( this == rhs )
            {
                return true;
            }
            if ( rhs instanceof com.google.inject.name.Named )
            {
                return value.equals( ( (com.google.inject.name.Named) rhs ).value() );
            }
            if ( rhs instanceof javax.inject.Named )
            {
                return value.equals( ( (javax.inject.Named) rhs ).value() );
            }
            return false;
        }

        @Override
        public String toString()
        {
            return "@" + javax.inject.Named.class.getName() + "(value=" + value + ")";
        }
    }
}
