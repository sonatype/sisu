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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.sonatype.inject.BeanEntry;

import com.google.inject.Binding;

/**
 * Copy-on-write cache mapping {@link Binding}s to {@link BeanEntry}s; optimized for common case of single entries.
 * Note: uses {@code ==} instead of {@code equals} to compare {@link Binding}s because we want referential equality.
 */
@SuppressWarnings( { "rawtypes", "unchecked" } )
final class BeanCache<Q extends Annotation, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final AtomicReference<Object> cache = new AtomicReference();

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
        final LazyBeanEntry newBean = new LazyBeanEntry( qualifier, binding, rank );

        Object o, n;

        /*
         * Compare-and-swap approach; avoids locking without missing any updates
         */
        do
        {
            o = cache.get();
            if ( null == o )
            {
                n = newBean; // most common case: adding the one (and-only) entry
            }
            else
            {
                if ( o instanceof LazyBeanEntry )
                {
                    final LazyBeanEntry entry = (LazyBeanEntry) o;
                    if ( binding == entry.binding )
                    {
                        return entry; // already added
                    }
                    n = new BoundBeans( entry, newBean );
                }
                else
                {
                    synchronized ( o )
                    {
                        BoundBeans<T> beans = (BoundBeans) o;
                        final BeanEntry oldBean = beans.map.get( binding );
                        if ( null != oldBean )
                        {
                            return oldBean; // already added
                        }
                        if ( beans.isImmutable )
                        {
                            beans = new BoundBeans( beans ); // copy-on-write
                        }
                        beans.map.put( binding, newBean );
                        if ( o == ( n = beans ) )
                        {
                            break; // no need to swap
                        }
                    }
                }
            }
        }
        while ( !cache.compareAndSet( o, n ) );

        return newBean;
    }

    /**
     * Retrieves the {@link BeanEntry} associated with the given {@link Binding} reference.
     * 
     * @param binding The binding
     * @return Associated bean entry
     */
    public BeanEntry<Q, T> get( final Binding<T> binding )
    {
        final Object o = cache.get();
        if ( null == o )
        {
            return null;
        }
        else if ( o instanceof LazyBeanEntry )
        {
            final LazyBeanEntry entry = (LazyBeanEntry) o;
            return binding == entry.binding ? entry : null;
        }
        final BoundBeans<T> beans = (BoundBeans) o;
        if ( beans.isImmutable )
        {
            return beans.map.get( binding );
        }
        synchronized ( o )
        {
            beans.isImmutable = true;
            return beans.map.get( binding );
        }
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
        final BoundBeans<T> beans = (BoundBeans) o;
        if ( beans.isImmutable )
        {
            return beans.map.keySet();
        }
        synchronized ( o )
        {
            beans.isImmutable = true;
            return beans.map.keySet();
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
                synchronized ( o )
                {
                    BoundBeans<T> beans = (BoundBeans) o;
                    if ( beans.isImmutable )
                    {
                        beans = new BoundBeans( beans ); // copy-on-write
                    }
                    oldBean = beans.map.remove( binding );
                    n = beans.map.isEmpty() ? null : beans;
                    if ( o == n )
                    {
                        return oldBean; // no need to swap
                    }
                }
            }
        }
        while ( !cache.compareAndSet( o, n ) );

        return oldBean;
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final class BoundBeans<T>
    {
        final IdentityHashMap<Binding<T>, LazyBeanEntry> map;

        volatile boolean isImmutable;

        BoundBeans( final LazyBeanEntry one, final LazyBeanEntry two )
        {
            map = new IdentityHashMap();
            map.put( one.binding, one );
            map.put( two.binding, two );
        }

        BoundBeans( final BoundBeans beans )
        {
            map = (IdentityHashMap) beans.map.clone();
        }
    }
}
