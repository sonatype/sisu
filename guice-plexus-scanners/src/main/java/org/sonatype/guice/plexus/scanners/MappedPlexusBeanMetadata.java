package org.sonatype.guice.plexus.scanners;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;

/**
 * Consumable {@link PlexusBeanMetadata} backed by maps with {@link BeanProperty} names as keys.
 */
final class MappedPlexusBeanMetadata
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

    MappedPlexusBeanMetadata( final Map<String, Configuration> configurationMap,
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