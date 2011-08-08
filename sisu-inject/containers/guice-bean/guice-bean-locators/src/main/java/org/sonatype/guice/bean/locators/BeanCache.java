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
            else if ( o instanceof LazyBeanEntry )
            {
                final LazyBeanEntry entry = (LazyBeanEntry) o;
                if ( binding == entry.binding )
                {
                    return entry; // already added
                }
                n = new BeanMap( entry, newBean );
            }
            else
            {
                /*
                 * For immutable maps use copy-on-write; otherwise must lock
                 */
                BeanMap<Q, T> map = (BeanMap) o;
                if ( map.isImmutable )
                {
                    final BeanEntry oldBean = map.get( binding );
                    if ( null != oldBean )
                    {
                        return oldBean;
                    }
                    // must copy-on-write
                    n = map = map.clone();
                    map.put( binding, newBean );
                }
                else
                {
                    synchronized ( map )
                    {
                        final BeanEntry oldBean = map.get( binding );
                        if ( null != oldBean )
                        {
                            return oldBean;
                        }
                        // modify the map directly
                        map.put( binding, newBean );
                        return newBean;
                    }
                }
            }
        }
        while ( !cache.compareAndSet( o, n ) );

        return newBean;
    }

    public void optimizeForReading()
    {
        final Object o = cache.get();
        if ( o instanceof BeanMap<?, ?> )
        {
            final BeanMap<Q, T> map = (BeanMap) o;
            if ( !map.isImmutable )
            {
                synchronized ( map )
                {
                    map.isImmutable = true; // optimize for reading
                }
            }
        }
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
        final BeanMap<Q, T> map = (BeanMap) o;
        if ( map.isImmutable )
        {
            return map.get( binding );
        }
        synchronized ( map )
        {
            return map.get( binding );
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
        final BeanMap<Q, T> map = (BeanMap) o;
        if ( map.isImmutable )
        {
            return map.keySet();
        }
        synchronized ( map )
        {
            return new ArrayList( map.keySet() ); // must take snapshot
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
                /*
                 * For immutable maps use copy-on-write; otherwise must lock
                 */
                BeanMap<Q, T> map = (BeanMap) o;
                if ( map.isImmutable )
                {
                    // try to avoid cloning if at all possible...
                    if ( null == ( oldBean = map.get( binding ) ) )
                    {
                        return null; // no such binding
                    }
                    else if ( map.size() > 1 )
                    {
                        // must copy-on-write
                        n = map = map.clone();
                        map.remove( binding );
                    }
                    else
                    {
                        n = null; // avoid clone when removing last entry
                    }
                }
                else
                {
                    synchronized ( map )
                    {
                        // modify the map directly
                        oldBean = map.remove( binding );
                        if ( map.size() > 0 )
                        {
                            return oldBean;
                        }
                        n = null; // avoid leaving empty maps around
                    }
                }
            }
        }
        while ( !cache.compareAndSet( o, n ) );

        return oldBean;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class BeanMap<Q extends Annotation, T>
        extends IdentityHashMap<Binding<T>, LazyBeanEntry<Q, T>>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final long serialVersionUID = 1L;

        // ----------------------------------------------------------------------
        // Implementation types
        // ----------------------------------------------------------------------

        volatile boolean isImmutable;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BeanMap( final LazyBeanEntry one, final LazyBeanEntry two )
        {
            put( one.binding, one );
            put( two.binding, two );
        }

        @Override
        public BeanMap clone()
        {
            final BeanMap clone = (BeanMap) super.clone();
            clone.isImmutable = false; // reset to default
            return clone;
        }
    }
}
