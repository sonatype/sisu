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
package org.sonatype.guice.plexus.config;

import java.util.List;
import java.util.Map;

/**
 * Supplies filtered maps/lists/wildcards of Plexus beans registered for a specific role {@code T}.
 */
public interface PlexusBeanRegistry<T>
{
    /**
     * Returns the Plexus hints of all the beans registered for the specific role.
     * 
     * @return List of Plexus hints
     */
    List<String> availableHints();

    /**
     * Returns a list of Plexus beans for the specific role, filtered by the given hints.
     * 
     * @param canonicalHints The Plexus hints to filter on
     * @return List of Plexus beans with the given hints
     */
    List<T> lookupList( final String... canonicalHints );

    /**
     * Returns a hint-map of Plexus beans for the specific role, filtered by the given hints.
     * 
     * @param canonicalHints The Plexus hints to filter on
     * @return Map of Plexus beans with the given hints
     */
    Map<String, T> lookupMap( final String... canonicalHints );

    /**
     * Returns the first Plexus bean registered for the specific role, regardless of hints.
     * 
     * @return Single Plexus bean
     */
    T lookupWildcard();
}