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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

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
        return injector.getInstance( DeferredInjector.class ).resume();
    }

    public static <T> List<T> asList( final Iterable<Entry<String, T>> entries )
    {
        return new IterableListAdapter<T>( entries );
    }

    public static <T> Map<String, T> asMap( final Iterable<Entry<String, T>> entries )
    {
        return new IterableMapAdapter<T>( entries );
    }
}
