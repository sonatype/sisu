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
package org.sonatype.guice.plexus.binders;

import org.sonatype.guice.plexus.config.PlexusBeanRegistry;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

/**
 * Utility methods that provide Plexus-style deferred injection on top of standard Guice.
 */
public final class PlexusGuice
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private PlexusGuice()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new PlexusGuice(); // keep Cobertura coverage happy
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Creates a new {@link Injector} and kicks-off an initial round of deferred injection.
     * 
     * @param modules The binding modules
     * @return Guice injector
     */
    public static Injector createInjector( final Module... modules )
    {
        return resumeInjections( Guice.createInjector( modules ) );
    }

    /**
     * Applies another round of deferred injections for the given Guice {@link Injector}.
     * 
     * @param injector The Guice injector
     * @return Guice injector
     */
    public static Injector resumeInjections( final Injector injector )
    {
        return injector.getInstance( DeferredInjector.class ).resumeInjections();
    }

    /**
     * Returns the Guice {@link PlexusBeanRegistry} binding {@link Key} for the given Plexus role.
     * 
     * @param role The Plexus role
     * @return Registry binding key for the given role
     */
    public static <T> Key<PlexusBeanRegistry<T>> registryKey( final Class<T> role )
    {
        return registryKey( TypeLiteral.get( role ) );
    }

    /**
     * Returns the Guice {@link PlexusComponentRegistry} binding {@link Key} for the given Plexus role.
     * 
     * @param role The Plexus role
     * @return Registry binding key for the given role
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Key<PlexusBeanRegistry<T>> registryKey( final TypeLiteral<T> role )
    {
        return (Key) Key.get( Types.newParameterizedType( GuicePlexusBeanRegistry.class, role.getType() ) );
    }
}
