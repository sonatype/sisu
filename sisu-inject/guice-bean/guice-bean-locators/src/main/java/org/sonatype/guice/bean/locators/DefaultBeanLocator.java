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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingExporter;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Default {@link MutableBeanLocator} that finds qualified beans across a dynamic group of {@link BindingExporter}s.
 */
@Singleton
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final RankedList<BindingExporter> exporters = new RankedList<BindingExporter>();

    private final Map<TypeLiteral, RankedBindings> bindingsCache = new HashMap<TypeLiteral, RankedBindings>();

    private final List<WatchedBeans> watchedBeans = new ArrayList<WatchedBeans>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized Iterable<BeanEntry> locate( final Key key )
    {
        final RankedBindings bindings = bindingsForType( key.getTypeLiteral() );
        final QualifiedBeans beans = new QualifiedBeans( key, bindings );
        bindings.addBeanCache( beans );
        return beans;
    }

    public synchronized void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        final WatchedBeans beans = new WatchedBeans( key, mediator, watcher );
        for ( final BindingExporter exporter : exporters )
        {
            beans.add( exporter, 0 );
        }
        watchedBeans.add( beans );
    }

    public synchronized void add( final BindingExporter exporter, final int rank )
    {
        exporters.insert( exporter, rank );
        distribute( BindingEvent.INSERT, exporter, rank );
    }

    public synchronized void remove( final BindingExporter exporter )
    {
        exporters.remove( exporter );
        distribute( BindingEvent.REMOVE, exporter, 0 );
    }

    public synchronized void clear()
    {
        exporters.clear();
        distribute( BindingEvent.CLEAR, null, 0 );
    }

    private final Map<Injector, BindingExporter> injectorExporters = new HashMap<Injector, BindingExporter>();

    private short injectorRank = Short.MIN_VALUE;

    @Inject
    @SuppressWarnings( "deprecation" )
    public synchronized void add( final Injector injector )
    {
        if ( !injectorExporters.containsKey( injector ) )
        {
            final BindingExporter exporter = new InjectorBindingExporter( injector, injectorRank );
            injectorExporters.put( injector, exporter );
            add( exporter, injectorRank++ );
        }
    }

    @SuppressWarnings( "deprecation" )
    public synchronized void remove( final Injector injector )
    {
        final BindingExporter exporter = injectorExporters.remove( injector );
        if ( null != exporter )
        {
            remove( exporter );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns the {@link RankedBindings} tracking the type; creates a new instance if one doesn't already exist.
     * 
     * @param type The required type
     * @return Sequence of ranked bindings
     */
    private <T> RankedBindings bindingsForType( final TypeLiteral<T> type )
    {
        RankedBindings bindings = bindingsCache.get( type );
        if ( null == bindings )
        {
            bindings = new RankedBindings( type, exporters );
            bindingsCache.put( type, bindings );
        }
        return bindings;
    }

    private void distribute( final BindingEvent event, final BindingExporter exporter, final int rank )
    {
        for ( final Iterator<RankedBindings> itr = bindingsCache.values().iterator(); itr.hasNext(); )
        {
            final RankedBindings bindings = itr.next();
            if ( bindings.isActive() )
            {
                event.apply( bindings, exporter, rank );
            }
            else
            {
                itr.remove();
            }
        }

        for ( int i = 0; i < watchedBeans.size(); i++ )
        {
            final WatchedBeans beans = watchedBeans.get( i );
            if ( beans.isActive() )
            {
                event.apply( beans, exporter, rank );
            }
            else
            {
                watchedBeans.remove( i-- );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static enum BindingEvent
    {
        INSERT
        {
            @Override
            void apply( final BindingDistributor distributor, final BindingExporter exporter, final int rank )
            {
                distributor.add( exporter, rank );
            }
        },
        REMOVE
        {
            @Override
            void apply( final BindingDistributor distributor, final BindingExporter exporter, final int rank )
            {
                distributor.remove( exporter );
            }
        },
        CLEAR
        {
            @Override
            void apply( final BindingDistributor distributor, final BindingExporter exporter, final int rank )
            {
                distributor.clear();
            }
        };

        abstract void apply( final BindingDistributor distributor, final BindingExporter exporter, final int rank );
    }
}
