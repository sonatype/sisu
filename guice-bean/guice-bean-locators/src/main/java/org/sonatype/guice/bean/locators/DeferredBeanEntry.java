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

import com.google.inject.Binding;

/**
 * Deferred caching bean {@link Entry} backed by a qualified {@link Provider}.
 */
final class DeferredBeanEntry<Q extends Annotation, T>
    implements Entry<Q, T>, Provider<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Binding<T> binding;

    private T bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DeferredBeanEntry( final Binding<T> binding )
    {
        this.binding = binding;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public Q getKey()
    {
        final Annotation ann = binding.getKey().getAnnotation();
        return (Q) ( null != ann ? ann : QualifiedBeans.DEFAULT_QUALIFIER );
    }

    public synchronized T getValue()
    {
        if ( null == bean )
        {
            bean = binding.getProvider().get();
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
    public String toString()
    {
        return getKey() + "=" + getValue();
    }
}
