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
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * {@link Iterable} sequence of qualified beans that uses a {@link Mediator} to send updates to watching objects.
 */
final class WatchedBeans<Q extends Annotation, T, W>
    extends QualifiedBeans<Q, T>
    implements Provider<QualifiedBeans<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Reference<W> watcherRef;

    private final Mediator<Q, T, W> mediator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    WatchedBeans( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        super( key );

        // use weak-ref to detect when watcher disappears
        this.watcherRef = new WeakReference<W>( watcher );
        this.mediator = mediator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public QualifiedBeans<Q, T> get()
    {
        // bean sequence disappears when watcher does
        return null != watcherRef.get() ? this : null;
    }

    @Override
    public synchronized List<Entry<Q, T>> add( final Injector injector )
    {
        final List<Entry<Q, T>> newBeans = super.add( injector );
        final W watcher = watcherRef.get();
        if ( null != watcher )
        {
            reportBeans( watcher, newBeans, BeanEvent.ADD );
        }
        return newBeans;
    }

    @Override
    public synchronized List<Entry<Q, T>> remove( final Injector injector )
    {
        final List<Entry<Q, T>> oldBeans = super.remove( injector );
        final W watcher = watcherRef.get();
        if ( null != watcher )
        {
            reportBeans( watcher, oldBeans, BeanEvent.REMOVE );
        }
        return oldBeans;
    }

    @Override
    public synchronized List<Entry<Q, T>> clear()
    {
        final List<Entry<Q, T>> oldBeans = super.clear();
        final W watcher = watcherRef.get();
        if ( null != watcher )
        {
            reportBeans( watcher, oldBeans, BeanEvent.REMOVE );
        }
        return oldBeans;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private enum BeanEvent
    {
        ADD
        {
            @Override
            public void send( final Mediator mediator, final DeferredBeanEntry bean, final Object watcher )
                throws Exception
            {
                mediator.add( bean.getKey(), bean, watcher );
            }
        },
        REMOVE
        {
            @Override
            public void send( final Mediator mediator, final DeferredBeanEntry bean, final Object watcher )
                throws Exception
            {
                mediator.remove( bean.getKey(), bean, watcher );
            }
        };

        abstract void send( final Mediator mediator, DeferredBeanEntry bean, Object watcher )
            throws Exception;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void reportBeans( final W watcher, final List<Entry<Q, T>> beans, final BeanEvent event )
    {
        for ( int i = 0, size = beans.size(); i < size; i++ )
        {
            try
            {
                event.send( mediator, (DeferredBeanEntry<Q, T>) beans.get( i ), watcher );
            }
            catch ( final Throwable e )
            {
                reportWatcherException( watcher, e );
            }
        }
    }

    /**
     * Reports the given watcher exception to the SLF4J logger if available; otherwise to JUL.
     * 
     * @param watcher The bean watcher
     * @param exception The exception
     */
    private void reportWatcherException( final Object watcher, final Throwable exception )
    {
        final String message = "Problem notifying watcher: " + watcher;
        final Class<?> mediatorType = mediator.getClass();
        try
        {
            org.slf4j.LoggerFactory.getLogger( mediatorType ).warn( message, exception );
        }
        catch ( final Throwable ignore )
        {
            Logger.getLogger( mediatorType.getName() ).log( Level.WARNING, message, exception );
        }
    }
}
