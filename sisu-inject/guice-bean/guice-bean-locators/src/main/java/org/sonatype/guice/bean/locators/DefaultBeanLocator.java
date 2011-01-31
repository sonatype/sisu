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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Default {@link MutableBeanLocator} that locates qualified beans across a dynamic group of {@link BindingPublisher}s.
 */
@Singleton
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final RankedList<BindingPublisher> publishers = new RankedList<BindingPublisher>();

    private final Map<TypeLiteral, RankedBindings> bindingsCache = new HashMap<TypeLiteral, RankedBindings>();

    private final List<WatchedBeans> watchedBeans = new ArrayList<WatchedBeans>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized Iterable<BeanEntry> locate( final Key key )
    {
        return new LocatedBeans( key, bindingsForType( key.getTypeLiteral() ) );
    }

    public synchronized void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        final WatchedBeans beans = new WatchedBeans( key, mediator, watcher );
        for ( int i = 0, size = publishers.size(); i < size; i++ )
        {
            beans.add( publishers.get( i ), 0 /* unused */);
        }
        watchedBeans.add( beans );
    }

    public void add( final Injector injector, final int rank )
    {
        add( new InjectorPublisher( injector, new DefaultRankingFunction( rank ) ), rank );
    }

    public void remove( final Injector injector )
    {
        remove( new InjectorPublisher( injector, null ) );
    }

    public synchronized void add( final BindingPublisher publisher, final int rank )
    {
        if ( !publishers.contains( publisher ) )
        {
            publishers.insert( publisher, rank );
            distribute( BindingEvent.ADD, publisher, rank );
        }
    }

    public synchronized void remove( final BindingPublisher publisher )
    {
        if ( publishers.remove( publisher ) )
        {
            distribute( BindingEvent.REMOVE, publisher, 0 );
        }
    }

    public synchronized void clear()
    {
        publishers.clear();
        distribute( BindingEvent.CLEAR, null, 0 );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Automatically publishes any {@link Injector} that contains a binding to this {@link BeanLocator}.
     * 
     * @param injector The injector
     */
    @Inject
    void autoPublish( final Injector injector )
    {
        final RankingFunction function = injector.getInstance( RankingFunction.class );
        add( new InjectorPublisher( injector, function ), function.maxRank() );
    }

    /**
     * Returns the {@link RankedBindings} tracking the given type; creates one if it doesn't already exist.
     * 
     * @param type The required type
     * @return Sequence of ranked bindings
     */
    private <T> RankedBindings<T> bindingsForType( final TypeLiteral<T> type )
    {
        RankedBindings<T> bindings = bindingsCache.get( type );
        if ( null == bindings )
        {
            bindings = new RankedBindings<T>( type, publishers );
            bindingsCache.put( type, bindings );
        }
        return bindings;
    }

    /**
     * Distributes the given binding event to interested parties.
     * 
     * @param event The binding event
     * @param publisher The optional publisher
     * @param rank The optional assigned rank
     */
    private void distribute( final BindingEvent event, final BindingPublisher publisher, final int rank )
    {
        for ( final Iterator<RankedBindings> itr = bindingsCache.values().iterator(); itr.hasNext(); )
        {
            final RankedBindings bindings = itr.next();
            if ( bindings.isActive() )
            {
                notify( bindings, event, publisher, rank );
            }
            else
            {
                itr.remove(); // cleanup up stale entries
            }
        }

        for ( int i = 0; i < watchedBeans.size(); i++ )
        {
            final WatchedBeans beans = watchedBeans.get( i );
            if ( beans.isActive() )
            {
                notify( beans, event, publisher, rank );
            }
            else
            {
                watchedBeans.remove( i-- ); // cleanup up stale entries
            }
        }
    }

    /**
     * Notifies the given distributor of the given binding event and its optional details.
     * 
     * @param distributor The distributor
     * @param event The binding event
     * @param publisher The optional publisher
     * @param rank The optional assigned rank
     */
    private static void notify( final BindingDistributor distributor, final BindingEvent event,
                                final BindingPublisher publisher, final int rank )
    {
        switch ( event )
        {
            case ADD:
                distributor.add( publisher, rank );
                break;
            case REMOVE:
                distributor.remove( publisher );
                break;
            case CLEAR:
                distributor.clear();
                break;
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static enum BindingEvent
    {
        ADD, REMOVE, CLEAR
    }
}
