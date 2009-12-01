package org.sonatype.guice.plexus.scanners;

import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;

/**
 * {@link PlexusBeanMetadata} backed by maps that use the {@link BeanProperty} name as the key.
 */
final class MappedPlexusBeanMetadata
    implements PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, Configuration> configuration;

    private final Map<String, Requirement> requirements;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MappedPlexusBeanMetadata( final Map<String, Configuration> configuration,
                              final Map<String, Requirement> requirements )
    {
        this.configuration = configuration;
        this.requirements = requirements;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        return configuration.get( property.getName() );
    }

    public Requirement getRequirement( final BeanProperty<?> property )
    {
        return requirements.get( property.getName() );
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
        addIfMissing( configuration, extraConfiguration );
        addIfMissing( requirements, extraRequirements );
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
    private static <K, V> void addIfMissing( final Map<K, V> primary, final Map<K, V> secondary )
    {
        for ( final Entry<K, V> e : secondary.entrySet() )
        {
            final K key = e.getKey();
            if ( !primary.containsKey( key ) )
            {
                primary.put( key, e.getValue() );
            }
        }
    }
}