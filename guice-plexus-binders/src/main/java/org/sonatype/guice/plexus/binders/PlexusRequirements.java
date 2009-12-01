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

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanRegistry;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Creates {@link Provider}s for properties with @{@link Requirement} metadata.
 */
final class PlexusRequirements
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeEncounter<?> encounter;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusRequirements( final TypeEncounter<?> encounter )
    {
        this.encounter = encounter;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link Provider} that provides Plexus components that match the given property requirement.
     * 
     * @param requirement The Plexus requirement
     * @param property The bean property
     * @return Provider that provides required Plexus components for the given property
     */
    @SuppressWarnings( "unchecked" )
    public <T> Provider<T> lookup( final Requirement requirement, final BeanProperty<T> property )
    {
        final String[] hints = Hints.canonicalHints( requirement );

        // deduce lookup from metadata + property details
        final TypeLiteral expectedType = property.getType();
        final TypeLiteral roleType = Roles.roleType( requirement, expectedType );
        final Class rawType = expectedType.getRawType();

        if ( Map.class == rawType )
        {
            final Provider<PlexusBeanRegistry<T>> registry = getRoleRegistry( roleType );
            return new Provider()
            {
                public Map<String, T> get()
                {
                    return registry.get().lookupMap( hints );
                }
            };
        }
        else if ( List.class == rawType )
        {
            final Provider<PlexusBeanRegistry<T>> registry = getRoleRegistry( roleType );
            return new Provider()
            {
                public List<T> get()
                {
                    return registry.get().lookupList( hints );
                }
            };
        }
        else if ( hints.length == 0 )
        {
            final Provider<PlexusBeanRegistry<T>> registry = getRoleRegistry( roleType );
            return new Provider()
            {
                public Object get()
                {
                    return registry.get().lookupWildcard();
                }
            };
        }

        return encounter.getProvider( Roles.componentKey( roleType, hints[0] ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns a {@link Provider} that can provide a {@link PlexusBeanRegistry} for the given role type.
     * 
     * @param role The reified Plexus role
     * @return Provider that provides a bean registry for the given role
     */
    private <T> Provider<PlexusBeanRegistry<T>> getRoleRegistry( final TypeLiteral<T> role )
    {
        return encounter.getProvider( PlexusGuice.registryKey( role ) );
    }
}
