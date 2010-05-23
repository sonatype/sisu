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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.inject.BeanMediator;

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

    private final List<Provider<GuiceBeans<?, ?>>> exposedBeans = new ArrayList<Provider<GuiceBeans<?, ?>>>();

    private final Set<Injector> injectors = new LinkedHashSet<Injector>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultBeanLocator()
    {
        // created outside injector
    }

    @Inject
    public DefaultBeanLocator( final Injector injector )
    {
        add( injector );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public synchronized Iterable locate( final Key key )
    {
        final GuiceBeans beans = initialize( new GuiceBeans( key ) );
        exposedBeans.add( new WeakBeanReference( beans ) );
        return beans;
    }

    @SuppressWarnings( "unchecked" )
    public synchronized void watch( final Key key, final BeanMediator mediator, final Object watcher )
    {
        exposedBeans.add( initialize( new WatchedBeans( key, mediator, watcher ) ) );
    }

    public synchronized void add( final Injector injector )
    {
        if ( null == injector || !injectors.add( injector ) )
        {
            return; // injector already tracked
        }
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final GuiceBeans<?, ?> beans = exposedBeans.get( i ).get();
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
            final GuiceBeans<?, ?> beans = exposedBeans.get( i ).get();
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

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Initializes a sequence of qualified beans based on the current list of {@link Injector}s.
     * 
     * @param beans The beans to initialize
     * @return Initialize beans
     */
    private <B extends GuiceBeans<?, ?>> B initialize( final B beans )
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
