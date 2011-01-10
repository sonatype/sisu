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

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.locators.spi.BindingSubscriber;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Binding;
import com.google.inject.Key;

/**
 * Provides dynamic {@link BeanEntry} notifications by tracking qualified {@link Binding}s.
 * 
 * @see BeanLocator#watch(Key, Mediator, Object)
 */
final class WatchedBeans<Q extends Annotation, T, W>
    implements BindingDistributor, BindingSubscriber
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Binding<T>, BeanEntry<Q, T>> beanCache = new IdentityHashMap<Binding<T>, BeanEntry<Q, T>>();

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
        publisher.subscribe( key.getTypeLiteral(), this );
    }

    public synchronized void remove( final BindingPublisher publisher )
    {
        publisher.unsubscribe( key.getTypeLiteral(), this );

        for ( final Iterator<Entry<Binding<T>, BeanEntry<Q, T>>> itr = beanCache.entrySet().iterator(); itr.hasNext(); )
        {
            final Entry<Binding<T>, BeanEntry<Q, T>> cacheEntry = itr.next();
            if ( publisher.contains( cacheEntry.getKey() ) )
            {
                notify( WatcherEvent.REMOVE, cacheEntry.getValue() );
                itr.remove();
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized void add( final Binding binding, final int rank )
    {
        final Q qualifier = (Q) strategy.qualifies( key, binding );
        if ( null != qualifier )
        {
            final BeanEntry<Q, T> bean = new LazyBeanEntry<Q, T>( qualifier, binding, rank );
            beanCache.put( binding, bean );
            notify( WatcherEvent.ADD, bean );
        }
    }

    @SuppressWarnings( "rawtypes" )
    public synchronized void remove( final Binding binding )
    {
        final BeanEntry<Q, T> bean = beanCache.remove( binding );
        if ( null != bean )
        {
            notify( WatcherEvent.REMOVE, bean );
        }
    }

    public synchronized void clear()
    {
        for ( final BeanEntry<Q, T> bean : beanCache.values() )
        {
            notify( WatcherEvent.REMOVE, bean );
        }
        beanCache.clear();
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
                Logs.warn( mediator.getClass(), "Problem notifying: " + watcher.getClass(), e );
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
