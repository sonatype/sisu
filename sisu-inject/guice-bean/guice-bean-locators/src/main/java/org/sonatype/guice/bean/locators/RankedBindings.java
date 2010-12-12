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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.sonatype.guice.bean.locators.spi.BindingExporter;
import org.sonatype.guice.bean.locators.spi.BindingImporter;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Ordered {@link Iterable} sequence that imports {@link Binding}s from {@link BindingExporter}s on demand.
 */
final class RankedBindings<T>
    implements Iterable<Binding<T>>, BindingImporter
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final TypeLiteral<T> type;

    final RankedList<BindingExporter> pendingExporters;

    final RankedList<Binding<T>> bindings = new RankedList<Binding<T>>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public RankedBindings( final TypeLiteral<T> type, final RankedList<BindingExporter> exporters )
    {
        this.type = type;
        this.pendingExporters = exporters.clone();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void add( final BindingExporter exporter, final int rank )
    {
        pendingExporters.insert( exporter, rank );
    }

    public void remove( final BindingExporter exporter )
    {
        // pending exporters haven't added bindings yet
        if ( !removeExact( pendingExporters, exporter ) )
        {
            exporter.remove( type, this );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void add( final Binding binding, final int rank )
    {
        bindings.insert( binding, rank );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void remove( final Binding binding )
    {
        removeExact( bindings, binding );
    }

    public Iterator<Binding<T>> iterator()
    {
        return new Itr();
    }

    public void clear()
    {
        pendingExporters.clear();
        bindings.clear();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Removes the exact given element from the list; uses identity comparison instead of equality.
     * 
     * @param list The list
     * @param element The element to remove
     * @return {@code true} if the element was removed; otherwise {@code false}
     */
    private static <S, T extends S> boolean removeExact( final RankedList<S> list, final T element )
    {
        synchronized ( list )
        {
            for ( int i = 0, size = list.size(); i < size; i++ )
            {
                if ( element == list.get( i ) )
                {
                    list.remove( i );
                    return true;
                }
            }
            return false;
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

        private final Iterator<Binding<T>> i = bindings.iterator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            synchronized ( pendingExporters )
            {
                // only import more bindings if it will affect the potential next element
                while ( pendingExporters.maxRank() > bindings.minRank() || !i.hasNext() )
                {
                    if ( !pendingExporters.isEmpty() )
                    {
                        pendingExporters.remove( 0 ).add( type, RankedBindings.this );
                    }
                    else
                    {
                        return false;
                    }
                }
                return true;
            }
        }

        public Binding<T> next()
        {
            if ( hasNext() )
            {
                return i.next(); // populated by hasNext()
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}