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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.guice.bean.reflect.TypeParameters;
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

    private final Lock lock = new ReentrantLock( true ); // popular lock, so make sure it's fair

    private final RankedSequence<BindingPublisher> publishers = new RankedSequence<BindingPublisher>();

    private final SoftMapping<TypeLiteral, RankedBindings> cachedBindings =
        new SoftMapping<TypeLiteral, RankedBindings>( 256, 0.75f, 8 );

    private final WeakSequence<WatchedBeans> watchedBeans = new WeakSequence<WatchedBeans>();

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
            lock.lock();
            try
            {
                if ( null == ( bindings = cachedBindings.get( type ) ) )
                {
                    cachedBindings.put( type, bindings = new RankedBindings( type, publishers ) );
                }
            }
            finally
            {
                lock.unlock();
            }
        }
        final boolean isImplicit = key.getAnnotationType() == null && TypeParameters.isImplicit( type );
        return new LocatedBeans( key, bindings, isImplicit ? implicitBindings : null );
    }

    public void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        lock.lock();
        try
        {
            final WatchedBeans beans = new WatchedBeans( key, mediator, watcher );
            for ( final BindingPublisher p : publishers.snapshot() )
            {
                p.subscribe( beans );
            }
            watchedBeans.link( beans, watcher );
        }
        finally
        {
            lock.unlock();
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
        lock.lock();
        try
        {
            if ( !publishers.contains( publisher ) )
            {
                Logs.debug( "Add publisher: {}", publisher, null );
                publishers.insert( publisher, rank );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.add( publisher, rank );
                }
                for ( final WatchedBeans beans : watchedBeans )
                {
                    publisher.subscribe( beans );
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void remove( final BindingPublisher publisher )
    {
        lock.lock();
        try
        {
            if ( publishers.remove( publisher ) )
            {
                Logs.debug( "Remove publisher: {}", publisher, null );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.remove( publisher );
                }
                for ( final WatchedBeans beans : watchedBeans )
                {
                    publisher.unsubscribe( beans );
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void clear()
    {
        lock.lock();
        try
        {
            for ( final BindingPublisher p : publishers.snapshot() )
            {
                remove( p );
            }
        }
        finally
        {
            lock.unlock();
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
