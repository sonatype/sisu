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

import org.sonatype.guice.bean.reflect.Logs;
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

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized Iterable<QualifiedBean> locate( final Key key, final Runnable listener )
    {
        final QualifiedBeans beans = null == listener ? new QualifiedBeans( key ) : new NotifyingBeans( key, listener );

        // store weak-reference to returned beans for automatic clean-up
        exposedBeans.add( new WeakBeanReference( populate( beans ) ) );

        return beans;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        // watched beans use a weak-reference to the watcher for automatic clean-up
        exposedBeans.add( populate( new WatchedBeans( key, mediator, watcher ) ) );
    }

    @Inject
    public synchronized void add( final Injector injector )
    {
        if ( null != injector && injectors.add( injector ) )
        {
            final String hash = Integer.toHexString( injector.hashCode() );
            Logs.debug( getClass(), "Adding Injector@{}: {}", hash, injector );
            sendEvent( InjectorEvent.ADD, injector );
        }
    }

    public synchronized void remove( final Injector injector )
    {
        if ( null != injector && injectors.remove( injector ) )
        {
            final String hash = Integer.toHexString( injector.hashCode() );
            Logs.debug( getClass(), "Removing Injector@{}:", hash, null );
            sendEvent( InjectorEvent.REMOVE, injector );
        }
    }

    public synchronized void clear()
    {
        Logs.debug( getClass(), "Clearing all Injectors", null, null );
        sendEvent( InjectorEvent.CLEAR, null );
        injectors.clear();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Enumerate the various {@link Injector} events that {@link QualifiedBeans} can receive.
     */
    @SuppressWarnings( "rawtypes" )
    private static enum InjectorEvent
    {
        ADD
        {
            @Override
            void send( final QualifiedBeans beans, final Injector injector )
            {
                beans.add( injector );
            }
        },
        REMOVE
        {
            @Override
            void send( final QualifiedBeans beans, final Injector injector )
            {
                beans.remove( injector );
            }
        },
        CLEAR
        {
            @Override
            void send( final QualifiedBeans beans, final Injector injector )
            {
                beans.clear();
            }
        };

        abstract void send( final QualifiedBeans beans, final Injector injector );
    }

    /**
     * Sends the given {@link Injector} event to all exposed beans known to the locator.
     * 
     * @param event The injector event
     * @param injector The injector
     */
    private void sendEvent( final InjectorEvent event, final Injector injector )
    {
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final QualifiedBeans<?, ?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                event.send( beans, injector );
            }
            else
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
    }

    /**
     * Populates a sequence of qualified beans based on the current group of {@link Injector}s.
     * 
     * @param beans The sequence to populate
     * @return The populated bean sequence
     */
    private <B extends QualifiedBeans<?, ?>> B populate( final B beans )
    {
        for ( final Injector injector : injectors )
        {
            beans.add( injector );
        }
        // take a moment to clear stale bean sequences
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
