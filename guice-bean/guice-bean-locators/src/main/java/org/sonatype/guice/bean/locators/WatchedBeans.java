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

import org.sonatype.inject.BeanMediator;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * {@link Iterable} sequence of qualified beans that uses a {@link BeanMediator} to send updates to a watching object.
 */
final class WatchedBeans<Q extends Annotation, T, W>
    extends GuiceBeans<Q, T>
    implements Provider<GuiceBeans<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Reference<W> watcherRef;

    private final BeanMediator<Q, T, W> mediator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    WatchedBeans( final Key<T> key, final BeanMediator<Q, T, W> mediator, final W watcher )
    {
        super( key );

        // use weak-ref to detect when watcher disappears
        this.watcherRef = new WeakReference<W>( watcher );
        this.mediator = mediator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public GuiceBeans<Q, T> get()
    {
        return null != watcherRef.get() ? this : null;
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    @Override
    synchronized List<Entry<Q, T>> add( final Injector injector )
    {
        final List<Entry<Q, T>> newBeans = super.add( injector );
        final W watcher = watcherRef.get();
        if ( null != watcher )
        {
            for ( int i = 0, size = newBeans.size(); i < size; i++ )
            {
                try
                {
                    @SuppressWarnings( "unchecked" )
                    final DeferredBeanEntry<Q, T> bean = (DeferredBeanEntry) newBeans.get( i );
                    mediator.add( bean.getKey(), bean, watcher );
                }
                catch ( final Throwable e )
                {
                    reportWatcherException( watcher, e );
                }
            }
        }
        return newBeans;
    }

    @Override
    synchronized List<Entry<Q, T>> remove( final Injector injector )
    {
        final List<Entry<Q, T>> oldBeans = super.remove( injector );
        final W watcher = watcherRef.get();
        if ( null != watcher )
        {
            for ( int i = 0, size = oldBeans.size(); i < size; i++ )
            {
                try
                {
                    @SuppressWarnings( "unchecked" )
                    final DeferredBeanEntry<Q, T> bean = (DeferredBeanEntry) oldBeans.get( i );
                    mediator.remove( bean.getKey(), bean, watcher );
                }
                catch ( final Throwable e )
                {
                    reportWatcherException( watcher, e );
                }
            }
        }
        return oldBeans;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Reports the given watcher exception to the SLF4J logger if available; otherwise to JUL.
     * 
     * @param watcher The bean watcher
     * @param exception The exception
     */
    private static void reportWatcherException( final Object watcher, final Throwable exception )
    {
        final String message = "Problem notifying watcher: " + watcher;
        try
        {
            org.slf4j.LoggerFactory.getLogger( BeanMediator.class ).warn( message, exception );
        }
        catch ( final Throwable ignore )
        {
            Logger.getLogger( BeanMediator.class.getName() ).log( Level.WARNING, message, exception );
        }
    }
}
