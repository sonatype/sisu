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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.spi.BindingExporter;
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

    private final Map<Object, TypeLiteral> keepAlive = new WeakHashMap<Object, TypeLiteral>();

    private final List<WatchedBeans> watchedBeans = new ArrayList<WatchedBeans>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized Iterable<QualifiedBean> locate( final Key key )
    {
        final RankedBindings bindings = bindingsForType( key.getTypeLiteral() );
        final QualifiedBeans beans = new QualifiedBeans( key, bindings );

        keepAlive.put( beans, bindings.type() );

        expungeStaleBindings();
        return beans;
    }

    public synchronized void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        final WatchedBeans beans = new WatchedBeans( key, mediator, watcher );
        for ( final BindingExporter exporter : exporters )
        {
            beans.add( exporter );
        }

        watchedBeans.add( beans );

        expungeStaleBindings();
    }

    public synchronized void add( final BindingExporter exporter, final int rank )
    {
        exporters.insert( exporter, rank );
        for ( final RankedBindings bindings : bindingsCache.values() )
        {
            bindings.add( exporter, rank );
        }
        for ( final WatchedBeans beans : watchedBeans )
        {
            beans.add( exporter );
        }
        expungeStaleBindings();
    }

    public synchronized void remove( final BindingExporter exporter )
    {
        exporters.remove( exporter );
        for ( final RankedBindings bindings : bindingsCache.values() )
        {
            bindings.remove( exporter );
        }
        for ( final WatchedBeans beans : watchedBeans )
        {
            beans.remove( exporter );
        }
        expungeStaleBindings();
    }

    public synchronized void clear()
    {
        for ( final RankedBindings bindings : bindingsCache.values() )
        {
            bindings.clear();
        }
        bindingsCache.clear();
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

    /**
     * Expunges any unused {@link RankedBindings}; relies on {@link WeakReference}s to keep-alive used elements.
     */
    private void expungeStaleBindings()
    {
        bindingsCache.keySet().retainAll( keepAlive.values() );
        for ( int i = 0; i < watchedBeans.size(); i++ )
        {
            if ( watchedBeans.get( i ).inactive() )
            {
                watchedBeans.remove( i-- );
            }
        }
    }
}
