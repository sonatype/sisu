/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.locators;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * {@link PlexusBeanLocator} that locates beans of various types from zero or more Guice {@link Injector}s.
 */
@Singleton
public final class GuiceBeanLocator
    implements PlexusBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Injector> injectors = new LinkedHashSet<Injector>();

    // keep track of all bean sequences that we've returned out in the past
    private final List<Reference<GuiceBeans<?>>> guiceBeans = new ArrayList<Reference<GuiceBeans<?>>>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Inject
    public synchronized void add( final Injector injector )
    {
        if ( null == injector || !injectors.add( injector ) )
        {
            return; // not a new injector, nothing to do
        }
        for ( int i = 0; i < guiceBeans.size(); i++ )
        {
            // inform existing sequences about the added injector
            final GuiceBeans<?> beans = guiceBeans.get( i ).get();
            if ( null != beans )
            {
                beans.add( injector );
            }
            else
            {
                // sequence has been GCd
                guiceBeans.remove( i-- );
            }
        }
    }

    public synchronized void remove( final Injector injector )
    {
        if ( null == injector || !injectors.remove( injector ) )
        {
            return; // not an old injector, nothing to do
        }
        for ( int i = 0; i < guiceBeans.size(); i++ )
        {
            // inform existing sequences about the removed injector
            final GuiceBeans<?> beans = guiceBeans.get( i ).get();
            if ( null != beans )
            {
                beans.remove( injector );
            }
            else
            {
                // sequence has been GCd
                guiceBeans.remove( i-- );
            }
        }
    }

    public synchronized <T> Iterable<Entry<String, T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        final GuiceBeans<T> beans;
        if ( hints.length == 0 )
        {
            // sequence of all beans with this type
            beans = new DefaultGuiceBeans<T>( role );
        }
        else
        {
            // sequence of hinted beans with this type
            beans = new HintedGuiceBeans<T>( role, hints );
        }

        // build up the current bean sequence
        for ( final Injector injector : injectors )
        {
            beans.add( injector );
        }

        for ( int i = 0; i < guiceBeans.size(); i++ )
        {
            // remove any previous GCd sequences
            if ( null == guiceBeans.get( i ).get() )
            {
                guiceBeans.remove( i-- );
            }
        }

        // record sequence so we can update it later on if necessary
        guiceBeans.add( new WeakReference<GuiceBeans<?>>( beans ) );

        return beans;
    }
}