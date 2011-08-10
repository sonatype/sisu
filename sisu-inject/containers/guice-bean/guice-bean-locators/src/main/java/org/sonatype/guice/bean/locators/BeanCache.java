/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.sonatype.inject.BeanEntry;

import com.google.inject.Binding;

/**
 * Atomic cache mapping {@link Binding}s to {@link BeanEntry}s; optimized for common case of single entries.<br>
 * Uses {@code ==} instead of {@code equals} to compare {@link Binding}s because we want referential equality.
 */
@SuppressWarnings( { "rawtypes", "unchecked" } )
final class BeanCache<Q extends Annotation, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final AtomicReference<Object> cache = new AtomicReference();

    private Map<Binding<T>, BeanEntry<Q, T>> preCache;

    private volatile boolean mutated;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Atomically creates a new {@link BeanEntry} for the given {@link Binding} reference.
     * 
     * @param qualifier The qualifier
     * @param binding The binding
     * @param rank The assigned rank
     * @return Associated bean entry
     */
    public BeanEntry<Q, T> create( final Q qualifier, final Binding<T> binding, final int rank )
    {
        LazyBeanEntry newBean;

        Object o, n;

        /*
         * Compare-and-swap approach; avoids locking without missing any updates
         */
        do
        {
            o = cache.get();
            if ( null == o )
            {
                // most common case: adding the one (and-only) entry
                n = newBean = new LazyBeanEntry( qualifier, binding, rank );
            }
            else if ( o instanceof LazyBeanEntry )
            {
                final LazyBeanEntry oldBean = (LazyBeanEntry) o;
                if ( binding == oldBean.binding )
                {
                    return oldBean;
                }
                n = createMap( oldBean, newBean = new LazyBeanEntry( qualifier, binding, rank ) );
            }
            else
            {
                synchronized ( this )
                {
                    final Map<Binding, LazyBeanEntry> map = (Map) o;
                    if ( null == ( newBean = map.get( binding ) ) )
                    {
                        map.put( binding, newBean = new LazyBeanEntry( qualifier, binding, rank ) );
                        mutated = true;
                    }
                    return newBean;
                }
            }
        }
        while ( !cache.compareAndSet( o, n ) );

        return newBean;
    }

    public Map<Binding<T>, BeanEntry<Q, T>> preCache()
    {
        if ( mutated )
        {
            synchronized ( this )
            {
                if ( mutated )
                {
                    preCache = (Map) ( (IdentityHashMap) cache.get() ).clone();
                    mutated = false;
                }
            }
        }
        return preCache;
    }

    /**
     * Retrieves the {@link Binding} references currently associated with {@link BeanEntry}s.
     * 
     * @return Associated bindings
     */
    public Iterable<Binding<T>> bindings()
    {
        final Object o = cache.get();
        if ( null == o )
        {
            return Collections.EMPTY_SET;
        }
        else if ( o instanceof LazyBeanEntry )
        {
            return Collections.singleton( ( (LazyBeanEntry<?, T>) o ).binding );
        }
        synchronized ( this )
        {
            return new ArrayList( ( (Map<Binding, BeanEntry>) o ).keySet() );
        }
    }

    /**
     * Removes the {@link BeanEntry} associated with the given {@link Binding} reference.
     * 
     * @param binding The binding
     * @return Associated bean entry
     */
    public BeanEntry<Q, T> remove( final Binding<T> binding )
    {
        LazyBeanEntry oldBean;

        Object o, n;

        /*
         * Compare-and-swap approach; avoids locking without missing any updates
         */
        do
        {
            o = cache.get();
            if ( null == o )
            {
                return null;
            }
            else if ( o instanceof LazyBeanEntry )
            {
                oldBean = (LazyBeanEntry) o;
                if ( binding != oldBean.binding )
                {
                    return null;
                }
                n = null; // clear single entry
            }
            else
            {
                synchronized ( this )
                {
                    oldBean = ( (Map<?, LazyBeanEntry>) o ).remove( binding );
                    if ( null != oldBean )
                    {
                        preCache = null;
                        mutated = true;
                    }
                    return oldBean;
                }
            }
        }
        while ( !cache.compareAndSet( o, n ) );

        return oldBean;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static Map createMap( final LazyBeanEntry one, final LazyBeanEntry two )
    {
        final Map map = new IdentityHashMap();
        map.put( one.binding, one );
        map.put( two.binding, two );
        return map;
    }
}
