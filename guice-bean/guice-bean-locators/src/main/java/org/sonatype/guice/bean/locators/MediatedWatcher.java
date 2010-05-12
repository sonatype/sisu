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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Utility class that uses a {@link Mediator} to send qualified bean updates to a watching object.
 */
final class MediatedWatcher<Q extends Annotation, T, W>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger( MediatedWatcher.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Mediator<Q, T, W> mediator;

    private final Reference<W> watcherRef;

    private final GuiceBeans<Q, T> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MediatedWatcher( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        this.mediator = mediator;

        // use weak-ref to detect when watcher disappears
        this.watcherRef = new WeakReference<W>( watcher );
        beans = new GuiceBeans<Q, T>( key );
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    /**
     * Pushes qualified beans belonging to the given injector to the watcher via the {@link Mediator}.
     * 
     * @param injector The new injector
     * @return {@code true} if the watcher is still watching; otherwise {@code false}
     */
    boolean push( final Injector injector )
    {
        final W watcher = watcherRef.get();
        if ( null == watcher )
        {
            return false; // watcher has been GC'd
        }
        final List<Entry<Q, T>> newBeans = beans.add( injector );
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
                logger.warn( "Problem notifying watcher", e );
            }
        }
        return true;
    }

    /**
     * Pulls qualified beans belonging to the given injector from the watcher via the {@link Mediator}.
     * 
     * @param injector The new injector
     * @return {@code true} if the watcher is still watching; otherwise {@code false}
     */
    boolean pull( final Injector injector )
    {
        final W watcher = watcherRef.get();
        if ( null == watcher )
        {
            return false; // watcher has been GC'd
        }
        final List<Entry<Q, T>> oldBeans = beans.remove( injector );
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
                logger.warn( "Problem notifying watcher", e );
            }
        }
        return true;
    }

    /**
     * Queries to see if the watcher is still watching out for qualified bean updates.
     * 
     * @return {@code true} if the watcher is still watching; otherwise {@code false}
     */
    boolean isWatching()
    {
        return watcherRef.get() != null;
    }
}
