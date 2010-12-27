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
import org.sonatype.guice.bean.locators.spi.BindingExporter;
import org.sonatype.guice.bean.locators.spi.BindingImporter;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Ordered {@link Iterable} sequence that imports {@link Binding}s from {@link BindingExporter}s on demand.
 */
final class RankedBindings<T>
    implements BindingDistributor, BindingImporter, Iterable<Binding<T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final RankedList<Binding<T>> bindings = new RankedList<Binding<T>>();

    final List<Reference<BeanCache<T>>> beanCaches = new ArrayList<Reference<BeanCache<T>>>();

    final RankedList<BindingExporter> pendingExporters;

    final TypeLiteral<T> type;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public RankedBindings( final TypeLiteral<T> type, final RankedList<BindingExporter> exporters )
    {
        this.type = type;

        pendingExporters = null != exporters ? exporters.clone() : new RankedList<BindingExporter>();
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

    public void publish( final BindingExporter exporter, final int rank )
    {
        synchronized ( pendingExporters )
        {
            pendingExporters.insert( exporter, rank );
        }
    }

    public void remove( final BindingExporter exporter )
    {
        synchronized ( pendingExporters )
        {
            // check if this exporter been activated
            if ( !pendingExporters.remove( exporter ) )
            {
                exporter.remove( type, this );
                flushBeanCaches();
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void publish( final Binding binding, final int rank )
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

    public Iterator<Binding<T>> iterator()
    {
        return new Itr();
    }

    public void clear()
    {
        synchronized ( pendingExporters )
        {
            pendingExporters.clear();
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

    /**
     * Custom {@link Iterator} that imports bindings from {@link BindingExporter}s on demand.
     */
    final class Itr
        implements Iterator<Binding<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final RankedList<Binding<T>>.Itr i = bindings.iterator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            if ( pendingExporters.size > 0 )
            {
                synchronized ( pendingExporters )
                {
                    while ( pendingExporters.size > 0 && pendingExporters.getRank( 0 ) >= i.peekNextRank() )
                    {
                        // be careful not to remove the exporter until after it is used
                        // otherwise another iterator could skip past the initial check
                        pendingExporters.get( 0 ).publish( type, RankedBindings.this );
                        pendingExporters.remove( 0 );
                    }
                }
            }
            return i.hasNext();
        }

        public Binding<T> next()
        {
            return i.next();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
