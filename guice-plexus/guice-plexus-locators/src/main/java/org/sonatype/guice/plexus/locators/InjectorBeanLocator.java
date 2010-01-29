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

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * {@link PlexusBeanLocator} that locates @{@link Named} beans based on explicit {@link Injector} bindings.
 */
public final class InjectorBeanLocator
    implements PlexusBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    public InjectorBeanLocator( final Injector injector )
    {
        this.injector = injector;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> Iterable<Entry<String, T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        if ( hints.length > 0 )
        {
            return new BeanEntriesByRoleHint<T>( injector, role, hints );
        }
        return new BeanEntriesByRole<T>( injector, role );
    }
}
