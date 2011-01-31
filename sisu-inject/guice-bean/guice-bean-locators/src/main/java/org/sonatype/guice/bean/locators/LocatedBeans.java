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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.sonatype.inject.BeanEntry;

import com.google.inject.Binding;
import com.google.inject.Key;

/**
 * Provides a sequence of {@link BeanEntry}s by iterating over qualified {@link Binding}s.
 * 
 * @see BeanLocator#locate(Key)
 */
final class LocatedBeans<Q extends Annotation, T>
    implements Iterable<BeanEntry<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Key<T> key;

    final RankedBindings<T> bindings;

    final QualifyingStrategy strategy;

    Map<Binding<T>, BeanEntry<Q, T>> beanCache;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LocatedBeans( final Key<T> key, final RankedBindings<T> bindings )
    {
        this.key = key;
        this.bindings = bindings;

        strategy = QualifyingStrategy.selectFor( key );

        bindings.linkToBeans( this );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized Iterator<BeanEntry<Q, T>> iterator()
    {
        return new Itr( null != beanCache ? new IdentityHashMap( beanCache ) : Collections.EMPTY_MAP );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Evict any stale {@link BeanEntry}s from this collection; an entry is stale if its binding is gone.
     * 
     * @param activeBindings The active bindings
     */
    synchronized void retainAll( final RankedList<Binding<T>> activeBindings )
    {
        if ( null != beanCache )
        {
            for ( final Binding<T> b : new ArrayList<Binding<T>>( beanCache.keySet() ) )
            {
                if ( activeBindings.indexOfThis( b ) < 0 )
                {
                    beanCache.remove( b );
                }
            }
        }
    }

    /**
     * Caches a {@link BeanEntry} for the given qualified binding; returns existing entry if already cached.
     * 
     * @param qualifier The qualifier
     * @param binding The binding
     * @param rank The assigned rank
     * @return Cached bean entry
     */
    synchronized BeanEntry<Q, T> cacheBean( final Q qualifier, final Binding<T> binding, final int rank )
    {
        if ( null == beanCache )
        {
            beanCache = new IdentityHashMap<Binding<T>, BeanEntry<Q, T>>();
        }
        BeanEntry<Q, T> bean = beanCache.get( binding );
        if ( null == bean )
        {
            bean = new LazyBeanEntry<Q, T>( qualifier, binding, rank );
            beanCache.put( binding, bean );
        }
        return bean;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BeanEntry} iterator that creates new elements from {@link Binding}s as required.
     */
    private final class Itr
        implements Iterator<BeanEntry<Q, T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final RankedBindings<T>.Itr itr = bindings.iterator();

        private final Map<Binding<T>, BeanEntry<Q, T>> readCache;

        private BeanEntry<Q, T> nextBean;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Itr( final Map<Binding<T>, BeanEntry<Q, T>> readCache )
        {
            this.readCache = readCache;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            if ( null != nextBean )
            {
                return true;
            }
            while ( itr.hasNext() )
            {
                final Binding<T> binding = itr.next();
                nextBean = readCache.get( binding );
                if ( null != nextBean )
                {
                    return true;
                }
                @SuppressWarnings( "unchecked" )
                final Q qualifier = (Q) strategy.qualifies( key, binding );
                if ( null != qualifier )
                {
                    nextBean = cacheBean( qualifier, binding, itr.rank() );
                    return true;
                }
            }
            return false;
        }

        public BeanEntry<Q, T> next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
                final BeanEntry<Q, T> bean = nextBean;
                nextBean = null;
                return bean;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
