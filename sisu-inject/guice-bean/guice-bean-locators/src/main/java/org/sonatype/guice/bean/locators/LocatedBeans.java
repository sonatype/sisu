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
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.sonatype.inject.BeanEntry;

import com.google.inject.Binding;
import com.google.inject.Key;

/**
 * Provides a sequence of {@link BeanEntry}s by searching qualified {@link Binding}s.
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

        bindings.addLocatedBeans( this );
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
    synchronized void retainAll( final Collection<Binding<T>> activeBindings )
    {
        if ( null != beanCache )
        {
            beanCache.keySet().retainAll( activeBindings );
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
