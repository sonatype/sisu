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

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Default {@link MutableBeanLocator} that finds qualified beans across a dynamic group of {@link BindingExporter}s.
 */
@Singleton
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Key<Integer> INJECTOR_RANKING_KEY = Key.get( Integer.class, Names.named( INJECTOR_RANKING ) );

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
            beans.publish( exporter, 0 );
        }
        watchedBeans.add( beans );
    }

    @SuppressWarnings( "deprecation" )
    public void publish( final Injector injector, final int rank )
    {
        publish( new InjectorBindingExporter( injector, rank ), rank );
    }

    public void remove( final Injector injector )
    {
        remove( new InjectorBindingExporter( injector, 0 ) );
    }

    public synchronized void publish( final BindingExporter exporter, final int rank )
    {
        if ( !exporters.contains( exporter ) )
        {
            exporters.insert( exporter, rank );
            distribute( BindingEvent.PUBLISH, exporter, rank );
        }
    }

    public synchronized void remove( final BindingExporter exporter )
    {
        if ( exporters.remove( exporter ) )
        {
            distribute( BindingEvent.REMOVE, exporter, 0 );
        }
    }

    public synchronized void clear()
    {
        exporters.clear();
        distribute( BindingEvent.CLEAR, null, 0 );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Inject
    @SuppressWarnings( "unused" )
    private void autoPublish( final Injector injector )
    {
        int rank = 0;
        final Binding<Integer> rankBinding = (Binding) injector.getBindings().get( INJECTOR_RANKING_KEY );
        if ( null != rankBinding )
        {
            rank = rankBinding.getProvider().get().intValue();
        }
        publish( injector, rank );
    }

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
        PUBLISH
        {
            @Override
            void apply( final BindingDistributor distributor, final BindingExporter exporter, final int rank )
            {
                distributor.publish( exporter, rank );
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
