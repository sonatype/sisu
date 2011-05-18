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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

    final RankedBindings<T> explicitBindings;

    final ImplicitBindings implicitBindings;

    final QualifyingStrategy strategy;

    @SuppressWarnings( "unchecked" )
    volatile Map<Binding<T>, BeanEntry<Q, T>> readCache = Collections.EMPTY_MAP;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LocatedBeans( final Key<T> key, final RankedBindings<T> explicitBindings, final ImplicitBindings implicitBindings )
    {
        this.key = key;

        this.explicitBindings = explicitBindings;
        this.implicitBindings = implicitBindings;

        strategy = QualifyingStrategy.selectFor( key );

        explicitBindings.linkToBeans( this );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<BeanEntry<Q, T>> iterator()
    {
        return new Itr();
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
        if ( readCache.size() > 0 )
        {
            @SuppressWarnings( { "rawtypes", "unchecked" } )
            final Map<Binding<T>, BeanEntry<Q, T>> tempCache = (Map) ( (IdentityHashMap) readCache ).clone();
            for ( final Iterator<Entry<Binding<T>, BeanEntry<Q, T>>> i = tempCache.entrySet().iterator(); i.hasNext(); )
            {
                if ( activeBindings.indexOfThis( i.next().getKey() ) < 0 )
                {
                    i.remove();
                }
            }
            readCache = tempCache;
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
        BeanEntry<Q, T> bean = readCache.get( binding );
        if ( null == bean )
        {
            bean = new LazyBeanEntry<Q, T>( qualifier, binding, rank );

            @SuppressWarnings( { "rawtypes", "unchecked" } )
            final Map<Binding<T>, BeanEntry<Q, T>> tempCache =
                readCache.size() > 0 ? (Map) ( (IdentityHashMap) readCache ).clone()
                                : new IdentityHashMap<Binding<T>, BeanEntry<Q, T>>();

            tempCache.put( binding, bean );
            readCache = tempCache;
        }
        return bean;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BeanEntry} iterator that creates new elements from {@link Binding}s as required.
     */
    final class Itr
        implements Iterator<BeanEntry<Q, T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final RankedBindings<T>.Itr itr = explicitBindings.iterator();

        private boolean checkImplicitBindings = implicitBindings != null;

        private BeanEntry<Q, T> nextBean;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
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
                final Q qualifier = (Q) strategy.qualifies( key, binding );
                if ( null != qualifier )
                {
                    nextBean = cacheBean( qualifier, binding, itr.rank() );
                    return true;
                }
            }
            if ( checkImplicitBindings )
            {
                // last-chance, see if we can locate a valid implicit binding somewhere
                final Binding<T> binding = implicitBindings.get( key.getTypeLiteral() );
                if ( null != binding )
                {
                    nextBean = cacheBean( (Q) QualifyingStrategy.DEFAULT_QUALIFIER, binding, Integer.MIN_VALUE );
                    return true;
                }
            }
            return false;
        }

        public BeanEntry<Q, T> next()
        {
            if ( hasNext() )
            {
                // no need to check this again
                checkImplicitBindings = false;

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
