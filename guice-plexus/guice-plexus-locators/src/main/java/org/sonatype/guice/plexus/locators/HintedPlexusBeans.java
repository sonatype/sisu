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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Specialized {@link PlexusBeans} implementation that iterates over beans of a given type according to named hints.
 */
final class HintedPlexusBeans<T>
    extends AbstractPlexusBeans<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeLiteral<T> role;

    private final String[] hints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HintedPlexusBeans( final TypeLiteral<T> role, final String[] hints )
    {
        this.role = role;
        this.hints = hints;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    InjectorBeans<T> lookupInjectorBeans( final Injector injector )
    {
        return new InjectorBeans<T>( injector, role, hints );
    }

    @Override
    List<PlexusBeanLocator.Bean<T>> sequenceBeans( final List<InjectorBeans<T>> beans )
    {
        // compile map of all known visible beans at this particular moment (can't build this map ahead of time)
        final Map<String, PlexusBeanLocator.Bean<T>> beanMap = new HashMap<String, PlexusBeanLocator.Bean<T>>();
        for ( int i = 0, size = beans.size(); i < size; i++ )
        {
            for ( final PlexusBeanLocator.Bean<T> b : beans.get( i ) )
            {
                beanMap.put( b.getKey(), b ); // later injector bindings overlay earlier ones
            }
        }

        // "copy-on-read" - select hinted beans from above map
        final List<PlexusBeanLocator.Bean<T>> hintedBeans = new ArrayList<PlexusBeanLocator.Bean<T>>( hints.length );
        for ( final String hint : hints )
        {
            final PlexusBeanLocator.Bean<T> bean = beanMap.get( hint );
            hintedBeans.add( null != bean ? bean : new MissingPlexusBean<T>( role, hint ) );
        }
        return hintedBeans;
    }
}
