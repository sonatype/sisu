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

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.locators.spi.BindingSubscriber;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Provides dynamic {@link BeanEntry} notifications by tracking qualified {@link Binding}s.
 * 
 * @see BeanLocator#watch(Key, Mediator, Object)
 */
final class WatchedBeans<Q extends Annotation, T, W>
    implements BindingDistributor, BindingSubscriber<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanCache<Q, T> beans = new BeanCache<Q, T>();

    private final Key<T> key;

    private final Mediator<Q, T, W> mediator;

    private final QualifyingStrategy strategy;

    private final Reference<W> watcherRef;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    WatchedBeans( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        this.key = key;
        this.mediator = mediator;

        strategy = QualifyingStrategy.selectFor( key );
        watcherRef = new WeakReference<W>( watcher );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized void add( final BindingPublisher publisher, final int rank )
    {
        publisher.subscribe( this );
    }

    public synchronized void remove( final BindingPublisher publisher )
    {
        publisher.unsubscribe( this );

        for ( final Binding<T> b : beans.bindings() )
        {
            if ( publisher.containsThis( b ) )
            {
                notify( WatcherEvent.REMOVE, beans.remove( b ) );
            }
        }
    }

    public TypeLiteral<T> type()
    {
        return key.getTypeLiteral();
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized void add( final Binding binding, final int rank )
    {
        final Q qualifier = (Q) strategy.qualifies( key, binding );
        if ( null != qualifier )
        {
            notify( WatcherEvent.ADD, beans.create( qualifier, binding, rank ) );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized void remove( final Binding binding )
    {
        final BeanEntry<Q, T> bean = beans.remove( binding );
        if ( null != bean )
        {
            notify( WatcherEvent.REMOVE, bean );
        }
    }

    public synchronized void clear()
    {
        for ( final Binding<T> b : beans.bindings() )
        {
            remove( b );
        }
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * @return {@code true} if these watched beans are still in use; otherwise {@code false}
     */
    boolean isActive()
    {
        return null != watcherRef.get();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Notifies the watching object of the given watcher event; uses the {@link Mediator} pattern.
     * 
     * @param event The watcher event
     * @param bean The bean entry
     */
    private void notify( final WatcherEvent event, final BeanEntry<Q, T> bean )
    {
        final W watcher = watcherRef.get();
        if ( null != watcher )
        {
            try
            {
                switch ( event )
                {
                    case ADD:
                        mediator.add( bean, watcher );
                        break;
                    case REMOVE:
                        mediator.remove( bean, watcher );
                        break;
                }
            }
            catch ( final Throwable e )
            {
                Logs.warn( "Problem mediating: {}", bean, e );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static enum WatcherEvent
    {
        ADD, REMOVE
    }
}
