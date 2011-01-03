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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.locators.spi.BindingSubscriber;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Ordered {@link Iterable} sequence of {@link Binding}s that queries {@link BindingPublisher}s on demand.
 */
final class RankedBindings<T>
    implements BindingDistributor, BindingSubscriber, Iterable<Binding<T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final RankedList<Binding<T>> bindings = new RankedList<Binding<T>>();

    final List<Reference<BeanCache<T>>> beanCaches = new ArrayList<Reference<BeanCache<T>>>();

    final RankedList<BindingPublisher> pendingPublishers;

    final TypeLiteral<T> type;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public RankedBindings( final TypeLiteral<T> type, final RankedList<BindingPublisher> publishers )
    {
        this.type = type;

        pendingPublishers = null != publishers ? publishers.clone() : new RankedList<BindingPublisher>();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public TypeLiteral<T> type()
    {
        return type;
    }

    public void addBeanCache( final BeanCache<T> cache )
    {
        synchronized ( beanCaches )
        {
            beanCaches.add( new WeakReference<BeanCache<T>>( cache ) );
        }
    }

    public boolean isActive()
    {
        boolean isActive = false;
        synchronized ( beanCaches )
        {
            for ( int i = 0; i < beanCaches.size(); i++ )
            {
                if ( null != beanCaches.get( i ).get() )
                {
                    isActive = true;
                }
                else
                {
                    beanCaches.remove( i-- );
                }
            }
        }
        return isActive;
    }

    public void add( final BindingPublisher publisher, final int rank )
    {
        synchronized ( pendingPublishers )
        {
            pendingPublishers.insert( publisher, rank );
        }
    }

    public void remove( final BindingPublisher publisher )
    {
        synchronized ( pendingPublishers )
        {
            // extra cleanup if already subscribed
            if ( !pendingPublishers.remove( publisher ) )
            {
                publisher.unsubscribe( type, this );
                synchronized ( bindings )
                {
                    for ( int i = 0; i < bindings.size(); i++ )
                    {
                        if ( publisher.contains( bindings.get( i ) ) )
                        {
                            bindings.remove( i-- );
                        }
                    }
                }
                flushBeanCaches();
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void add( final Binding binding, final int rank )
    {
        synchronized ( bindings )
        {
            bindings.insert( binding, rank );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void remove( final Binding binding )
    {
        synchronized ( bindings )
        {
            final int index = bindings.indexOfThis( binding );
            if ( index >= 0 )
            {
                bindings.remove( index );
            }
        }
    }

    public Itr iterator()
    {
        return new Itr();
    }

    public void clear()
    {
        synchronized ( pendingPublishers )
        {
            pendingPublishers.clear();
        }
        synchronized ( bindings )
        {
            bindings.clear();
        }
        flushBeanCaches();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void flushBeanCaches()
    {
        synchronized ( beanCaches )
        {
            for ( int i = 0, size = beanCaches.size(); i < size; i++ )
            {
                final BeanCache<T> cache = beanCaches.get( i ).get();
                if ( null != cache )
                {
                    cache.keepAlive( bindings );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    final class Itr
        implements Iterator<Binding<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final RankedList<Binding<T>>.Itr itr = bindings.iterator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            if ( pendingPublishers.size() > 0 )
            {
                synchronized ( pendingPublishers )
                {
                    while ( pendingPublishers.size() > 0 && pendingPublishers.getRank( 0 ) > itr.peekNextRank() )
                    {
                        // be careful not to remove the pending publisher until after it's used
                        // otherwise another iterator could skip past the initial size() check
                        pendingPublishers.get( 0 ).subscribe( type, RankedBindings.this );
                        pendingPublishers.remove( 0 );
                    }
                }
            }
            return itr.hasNext();
        }

        public Binding<T> next()
        {
            return itr.next();
        }

        public int rank()
        {
            return itr.rank();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
