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
import java.util.Map;

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingExporter;
import org.sonatype.guice.bean.locators.spi.BindingImporter;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Binding;
import com.google.inject.Key;

final class WatchedBeans<Q extends Annotation, T, W>
    implements BindingDistributor, BindingImporter
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

    public boolean isActive()
    {
        return null != watcherRef.get();
    }

    public void publish( final BindingExporter exporter, final int rank )
    {
        exporter.publish( key.getTypeLiteral(), this );
    }

    public void remove( final BindingExporter exporter )
    {
        exporter.remove( key.getTypeLiteral(), this );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized void publish( final Binding binding, final int rank )
    {
        final Q qualifier = (Q) strategy.qualifies( key, binding );
        if ( null != qualifier )
        {
            final W watcher = watcherRef.get();
            if ( null != watcher )
            {
                final BeanEntry<Q, T> bean = new LazyQualifiedBean<Q, T>( qualifier, binding, rank );
                beanCache.put( binding, bean );
                try
                {
                    mediator.add( bean, watcher );
                }
                catch ( final Throwable e )
                {
                    Logs.warn( mediator.getClass(), "Problem notifying: " + watcher.getClass(), e );
                }
            }
        }
    }

    @SuppressWarnings( "rawtypes" )
    public synchronized void remove( final Binding binding )
    {
        final BeanEntry<Q, T> bean = beanCache.remove( binding );
        if ( null != bean )
        {
            final W watcher = watcherRef.get();
            if ( null != watcher )
            {
                try
                {
                    mediator.remove( bean, watcher );
                }
                catch ( final Throwable e )
                {
                    Logs.warn( mediator.getClass(), "Problem notifying: " + watcher.getClass(), e );
                }
            }
        }
    }

    public synchronized void clear()
    {
        for ( final Binding<T> binding : beanCache.keySet().toArray( new Binding[beanCache.size()] ) )
        {
            remove( binding );
        }
    }
}
