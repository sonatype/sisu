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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;

final class MediatedWatcher<Q extends Annotation, T, W>
{
    private static final Logger logger = LoggerFactory.getLogger( MediatedWatcher.class );

    private final Key<T> key;

    private final Mediator<Q, T, W> mediator;

    private final Reference<W> watcherRef;

    MediatedWatcher( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        this.key = key;
        this.mediator = mediator;
        this.watcherRef = new WeakReference<W>( watcher );
    }

    void add( final Injector injector )
    {
        final W watcher = watcherRef.get();
        if ( null == watcher )
        {
            return;
        }
        final InjectorBeans<Q, T> beans = new InjectorBeans<Q, T>( injector, key );
        for ( int i = 0, size = beans.size(); i < size; i++ )
        {
            try
            {
                mediator.add( beans.get( i ), watcher );
            }
            catch ( final Throwable e )
            {
                logger.warn( "Problem notifying watcher", e );
            }
        }
    }

    void remove( final Injector injector )
    {
        final W watcher = watcherRef.get();
        if ( null == watcher )
        {
            return;
        }
        final InjectorBeans<Q, T> beans = new InjectorBeans<Q, T>( injector, key );
        for ( int i = 0, size = beans.size(); i < size; i++ )
        {
            try
            {
                mediator.remove( beans.get( i ), watcher );
            }
            catch ( final Throwable e )
            {
                logger.warn( "Problem notifying watcher", e );
            }
        }
    }

    boolean isWatching()
    {
        return watcherRef.get() != null;
    }
}
