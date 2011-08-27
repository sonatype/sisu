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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.guice.bean.reflect.Soft;
import org.sonatype.guice.bean.reflect.TypeParameters;
import org.sonatype.guice.bean.reflect.Weak;
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

    private final RankedSequence<BindingPublisher> publishers = new RankedSequence<BindingPublisher>();

    private final Map<TypeLiteral, RankedBindings> cachedBindings = Soft.concurrentValues( 256, 8 );

    // reverse mapping; can't use watcher as key since it may not be unique
    private final Map<WatchedBeans, Object> cachedWatchers = Weak.values();

    private final ImplicitBindings implicitBindings = new ImplicitBindings( publishers );

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterable<BeanEntry> locate( final Key key )
    {
        final TypeLiteral type = key.getTypeLiteral();
        RankedBindings bindings = cachedBindings.get( type );
        if ( null == bindings )
        {
            synchronized ( publishers )
            {
                if ( null == ( bindings = cachedBindings.get( type ) ) )
                {
                    cachedBindings.put( type, bindings = new RankedBindings( type, publishers ) );
                }
            }
        }
        final boolean isImplicit = key.getAnnotationType() == null && TypeParameters.isImplicit( type );
        return new LocatedBeans( key, bindings, isImplicit ? implicitBindings : null );
    }

    public void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        synchronized ( publishers )
        {
            final WatchedBeans beans = new WatchedBeans( key, mediator, watcher );
            for ( final BindingPublisher p : publishers.snapshot() )
            {
                p.subscribe( beans );
            }
            cachedWatchers.put( beans, watcher );
        }
    }

    public void add( final Injector injector, final int rank )
    {
        add( new InjectorPublisher( injector, new DefaultRankingFunction( rank ) ), rank );
    }

    public void remove( final Injector injector )
    {
        remove( new InjectorPublisher( injector, null ) );
    }

    public void add( final BindingPublisher publisher, final int rank )
    {
        synchronized ( publishers )
        {
            if ( !publishers.contains( publisher ) )
            {
                Logs.debug( "Add publisher: {}", publisher, null );
                publishers.insert( publisher, rank );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.add( publisher, rank );
                }
                for ( final WatchedBeans beans : cachedWatchers.keySet() )
                {
                    publisher.subscribe( beans );
                }
            }
        }
    }

    public void remove( final BindingPublisher publisher )
    {
        synchronized ( publishers )
        {
            if ( publishers.remove( publisher ) )
            {
                Logs.debug( "Remove publisher: {}", publisher, null );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.remove( publisher );
                }
                for ( final WatchedBeans beans : cachedWatchers.keySet() )
                {
                    publisher.unsubscribe( beans );
                }
            }
        }
    }

    public void clear()
    {
        synchronized ( publishers )
        {
            for ( final BindingPublisher p : publishers.snapshot() )
            {
                remove( p );
            }
        }
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
        staticAutoPublish( this, injector );
    }

    @Inject
    static void staticAutoPublish( final MutableBeanLocator locator, final Injector injector )
    {
        final RankingFunction function = injector.getInstance( RankingFunction.class );
        locator.add( new InjectorPublisher( injector, function ), function.maxRank() );
    }
}
