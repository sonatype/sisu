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
import java.util.Collections;
import java.util.List;

import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Default {@link GuiceBeans} implementation that iterates over all beans of a given type.
 */
final class DefaultGuiceBeans<T>
    extends AbstractGuiceBeans<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeLiteral<T> role;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DefaultGuiceBeans( final TypeLiteral<T> role )
    {
        this.role = role;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    InjectorBeans<T> discoverInjectorBeans( final Injector injector )
    {
        return new InjectorBeans<T>( injector, role );
    }

    @Override
    List<PlexusBeanLocator.Bean<T>> getBeans( final List<InjectorBeans<T>> visibleBeans )
    {
        if ( visibleBeans.isEmpty() )
        {
            return Collections.emptyList();
        }
        // "copy-on-read" - concatenate all beans
        final List<PlexusBeanLocator.Bean<T>> combinedBeans = new ArrayList<PlexusBeanLocator.Bean<T>>();
        for ( int i = 0, size = visibleBeans.size(); i < size; i++ )
        {
            combinedBeans.addAll( visibleBeans.get( i ) );
        }
        return combinedBeans;
    }
}
