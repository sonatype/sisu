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

import static org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS;
import static org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.ReferenceType.STRONG;
import static org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.ReferenceType.WEAK;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.Option;
import org.sonatype.guice.bean.locators.spi.BindingExporter;
import org.sonatype.guice.bean.locators.spi.BindingImporter;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.inject.Mediator;

import com.google.inject.Binding;
import com.google.inject.Key;

final class WatchedBeans<Q extends Annotation, T, W>
    implements BindingImporter
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final EnumSet<Option> IDENTITY = EnumSet.of( IDENTITY_COMPARISONS );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Key<T> key;

    private final Mediator<Q, T, W> mediator;

    private final QualifyingStrategy strategy;

    private final WeakReference<W> watcherRef;

    private final ConcurrentMap<Binding<T>, QualifiedBean<Q, T>> beanMap =
        new ConcurrentReferenceHashMap<Binding<T>, QualifiedBean<Q, T>>( WEAK, STRONG, IDENTITY );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    WatchedBeans( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        this.key = key;
        this.mediator = mediator;

        strategy = QualifiedBeans.selectQualifyingStrategy( key );
        watcherRef = new WeakReference<W>( watcher );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean inactive()
    {
        return null == watcherRef.get();
    }

    public void add( final BindingExporter exporter )
    {
        exporter.add( key.getTypeLiteral(), this );
    }

    public void remove( final BindingExporter exporter )
    {
        exporter.remove( key.getTypeLiteral(), this );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void add( final Binding binding, final int rank )
    {
        final Q qualifier = (Q) strategy.qualifies( key, binding );
        if ( null != qualifier )
        {
            final W watcher = watcherRef.get();
            if ( null != watcher )
            {
                final QualifiedBean<Q, T> bean = new LazyQualifiedBean<Q, T>( qualifier, binding );
                beanMap.put( binding, bean );
                try
                {
                    mediator.add( qualifier, bean, watcher );
                }
                catch ( final Throwable e )
                {
                    Logs.warn( mediator.getClass(), "Problem notifying: " + watcher.getClass(), e );
                }
            }
        }
    }

    @SuppressWarnings( "rawtypes" )
    public void remove( final Binding binding )
    {
        final QualifiedBean<Q, T> bean = beanMap.get( binding );
        if ( null != bean )
        {
            final W watcher = watcherRef.get();
            if ( null != watcher )
            {
                try
                {
                    mediator.remove( bean.getKey(), bean, watcher );
                }
                catch ( final Throwable e )
                {
                    Logs.warn( mediator.getClass(), "Problem notifying: " + watcher.getClass(), e );
                }
            }
        }
    }

    public void clear()
    {
        for ( final Binding<T> binding : beanMap.keySet() )
        {
            remove( binding );
        }
    }
}
