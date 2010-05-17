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
import java.util.Map.Entry;

import javax.inject.Provider;

/**
 * Deferred caching bean {@link Entry} backed by a qualified {@link Provider}.
 */
final class DeferredBeanEntry<Q extends Annotation, T>
    implements Entry<Q, T>, Provider<T>, Comparable<DeferredBeanEntry<?, ?>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Q qualifier;

    private Provider<T> provider;

    private T bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DeferredBeanEntry( final Q qualifier, final Provider<T> provider )
    {
        this.qualifier = qualifier;
        this.provider = provider;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Q getKey()
    {
        return qualifier;
    }

    public synchronized T getValue()
    {
        if ( null != provider )
        {
            // cache bean result
            bean = provider.get();
            provider = null;
        }
        return bean;
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public T get()
    {
        return getValue();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( rhs instanceof DeferredBeanEntry<?, ?> )
        {
            return 0 == compareTo( (DeferredBeanEntry<?, ?>) rhs );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return qualifier.hashCode();
    }

    public int compareTo( final DeferredBeanEntry<?, ?> rhs )
    {
        if ( qualifier == rhs.qualifier )
        {
            return 0;
        }
        if ( BeanLocator.DEFAULT == qualifier )
        {
            return -1;
        }
        if ( BeanLocator.DEFAULT == rhs.qualifier )
        {
            return 1;
        }
        return this.qualifier.toString().compareTo( rhs.qualifier.toString() );
    }

    @Override
    public String toString()
    {
        return qualifier + "=" + provider;
    }
}
