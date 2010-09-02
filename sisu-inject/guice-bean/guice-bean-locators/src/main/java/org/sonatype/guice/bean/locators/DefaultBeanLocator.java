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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Default {@link BeanLocator} that locates qualified beans across a dynamic group of {@link Injector}s.
 */
@Singleton
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Provider<? extends QualifiedBeans<?, ?>>> exposedBeans =
        new ArrayList<Provider<? extends QualifiedBeans<?, ?>>>();

    private final Set<Injector> injectors = new LinkedHashSet<Injector>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized <Q extends Annotation, T> Iterable<QualifiedBean<Q, T>> locate( final Key<T> key,
                                                                                        final Runnable notify )
    {
        final QualifiedBeans<Q, T> beans =
            initialize( null == notify ? new QualifiedBeans<Q, T>( key ) : new NotifyingBeans<Q, T>( key, notify ) );
        exposedBeans.add( new WeakBeanReference<Q, T>( beans ) );
        return beans;
    }

    public synchronized <Q extends Annotation, T, W> void watch( final Key<T> key, final Mediator<Q, T, W> mediator,
                                                                 final W watcher )
    {
        exposedBeans.add( initialize( new WatchedBeans<Q, T, W>( key, mediator, watcher ) ) );
    }

    @Inject
    public synchronized void add( final Injector injector )
    {
        if ( null == injector || !injectors.add( injector ) )
        {
            return; // injector already tracked
        }
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final QualifiedBeans<?, ?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.add( injector ); // update exposed sequence to include new injector
            }
            else
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
    }

    public synchronized void remove( final Injector injector )
    {
        if ( null == injector || !injectors.remove( injector ) )
        {
            return; // injector wasn't tracked
        }
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final QualifiedBeans<?, ?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.remove( injector ); // update exposed sequence to ignore old injector
            }
            else
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
    }

    public synchronized void clear()
    {
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final QualifiedBeans<?, ?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.clear();
            }
            else
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
        injectors.clear();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Initializes a sequence of qualified beans based on the current list of {@link Injector}s.
     * 
     * @param beans The beans to initialize
     * @return Initialize beans
     */
    private <B extends QualifiedBeans<?, ?>> B initialize( final B beans )
    {
        for ( final Injector injector : injectors )
        {
            beans.add( injector );
        }
        // take a moment to check previous sequences
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            if ( null == exposedBeans.get( i ).get() )
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
        return beans;
    }
}
