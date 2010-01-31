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

    final List<Injector> injectors = new ArrayList<Injector>( 1024 ); // TODO: RW-lock?

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Inject
    public void add( final Injector injector )
    {
        injectors.add( injector );
    }

    @Inject
    public void remove( final Injector injector )
    {
        final int index = injectors.indexOf( injector );
        if ( index >= 0 )
        {
            injectors.set( index, null );
        }
    }

    public <T> Iterable<Entry<String, T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        if ( hints.length > 0 )
        {
            return new BeanEntriesByRoleHint<T>( injectors, role, hints );
        }
        return new BeanEntriesByRole<T>( injectors, role );
    }
}