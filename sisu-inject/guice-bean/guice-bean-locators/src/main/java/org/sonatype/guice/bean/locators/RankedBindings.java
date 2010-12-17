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
        pendingExporters = null != exporters ? exporters.clone() : new RankedList<BindingExporter>();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public TypeLiteral<T> type()
    {
        return type;
    }

    public void add( final BindingExporter exporter, final int rank )
    {
        pendingExporters.insert( exporter, rank );
    }

    public void remove( final BindingExporter exporter )
    {
        // check if this exporter been activated
        if ( !pendingExporters.remove( exporter ) )
        {
            exporter.remove( type, this );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void add( final Binding binding, final int rank )
    {
        bindings.insert( binding, rank );
    }

    @SuppressWarnings( "rawtypes" )
    public void remove( final Binding binding )
    {
        bindings.remove( binding );
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
                        pendingExporters.get( 0 ).add( type, RankedBindings.this );
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