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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * 
 */
@Singleton
public final class GuiceBeanLocator
    implements PlexusBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final List<PlexusBeanLocator> locators = new ArrayList<PlexusBeanLocator>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    GuiceBeanLocator( final Injector rootInjector )
    {
        locators.add( new InjectorBeanLocator( rootInjector ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized void add( final PlexusBeanLocator locator )
    {
        locators.add( locator );
    }

    public synchronized void remove( final PlexusBeanLocator locator )
    {
        locators.remove( locator );
    }

    @SuppressWarnings( "unchecked" )
    public synchronized <T> Iterable<Entry<String, T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        final Iterable[] iterables = new Iterable[locators.size()];
        for ( int i = 0; i < iterables.length; i++ )
        {
            iterables[i] = locators.get( i ).locate( role, hints );
        }
        return new RoundRobinIterable( iterables );
    }
}