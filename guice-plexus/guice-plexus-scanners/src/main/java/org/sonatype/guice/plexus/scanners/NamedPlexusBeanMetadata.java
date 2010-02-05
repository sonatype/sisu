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
package org.sonatype.guice.plexus.scanners;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;

/**
 * Consumable {@link PlexusBeanMetadata} that uses {@link BeanProperty} names as keys.
 */
final class NamedPlexusBeanMetadata
    implements PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private Map<String, Configuration> configurationMap = Collections.emptyMap();

    private Map<String, Requirement> requirementMap = Collections.emptyMap();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NamedPlexusBeanMetadata( final Map<String, Configuration> configurationMap,
                             final Map<String, Requirement> requirementMap )
    {
        merge( configurationMap, requirementMap );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean isEmpty()
    {
        return configurationMap.isEmpty() && requirementMap.isEmpty();
    }

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        return configurationMap.remove( property.getName() );
    }

    public Requirement getRequirement( final BeanProperty<?> property )
    {
        return requirementMap.remove( property.getName() );
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    /**
     * Merges the given configuration and requirements with the current metadata, without overwriting existing entries.
     * 
     * @param extraConfiguration The extra configuration
     * @param extraRequirements The extra requirements
     */
    void merge( final Map<String, Configuration> extraConfiguration, final Map<String, Requirement> extraRequirements )
    {
        configurationMap = addIfMissing( configurationMap, extraConfiguration );
        requirementMap = addIfMissing( requirementMap, extraRequirements );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Looks for keys that exist in the secondary map, but not the primary, and adds their mappings to the primary map.
     * 
     * @param primary The primary map
     * @param secondary The secondary map
     */
    private static <K, V> Map<K, V> addIfMissing( final Map<K, V> primary, final Map<K, V> secondary )
    {
        // nothing to add?
        if ( secondary.isEmpty() )
        {
            return primary;
        }

        // nothing to merge?
        if ( primary.isEmpty() )
        {
            return secondary;
        }

        // primary mappings always override secondary mappings
        final Map<K, V> tempMap = new HashMap<K, V>( secondary );
        tempMap.putAll( primary );
        return tempMap;
    }
}