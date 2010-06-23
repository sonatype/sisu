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
package org.sonatype.guice.plexus.config;

import com.google.inject.Injector;

/**
 * Mutable {@link PlexusBeanLocator} that tracks zero or more {@link Injector}s.
 */
public interface MutablePlexusBeanLocator
    extends PlexusBeanLocator
{
    /**
     * Adds Plexus beans belonging to the given injector to any exposed sequences.
     * 
     * @param injector The new injector
     */
    void add( Injector injector );

    /**
     * Removes Plexus beans belonging to the given injector from any exposed sequences.
     * 
     * @param injector The old injector
     */
    void remove( Injector injector );

    /**
     * Removes all known Plexus beans from any exposed sequences.
     */
    void clear();
}
