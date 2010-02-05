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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.Roles;

/**
 * Enhanced Plexus component map with additional book-keeping.
 */
final class PlexusComponentMap
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Logger logger = LoggerFactory.getLogger( PlexusComponentMap.class );

    private final Map<String, String> strategies = new HashMap<String, String>();

    private final Map<Component, DeferredClass<?>> components = new HashMap<Component, DeferredClass<?>>();

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusComponentMap( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    /**
     * @return Current class space
     */
    ClassSpace getSpace()
    {
        return space;
    }

    /**
     * Selects the given instantiation strategy for the given Plexus component.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @param instantiationStrategy The selected instantiation strategy
     */
    void selectStrategy( final String role, final String hint, final String instantiationStrategy )
    {
        strategies.put( Roles.canonicalRoleHint( role, hint ), instantiationStrategy );
    }

    /**
     * Checks the selected instantiation strategy for the given Plexus component.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @param instantiationStrategy The default instantiation strategy
     * @return Selected instantiation strategy
     */
    String checkStrategy( final String role, final String hint, final String instantiationStrategy )
    {
        final String roleHintKey = Roles.canonicalRoleHint( role, hint );
        final String selectedStrategy = strategies.get( roleHintKey );
        if ( null == selectedStrategy )
        {
            strategies.put( roleHintKey, instantiationStrategy );
            return instantiationStrategy;
        }
        return selectedStrategy;
    }

    /**
     * Attempts to load the given Plexus role, checking its constructor dependencies also exist.
     * 
     * @param role The Plexus role
     * @param implementation The component implementation
     * @return Loaded Plexus role
     */
    Class<?> loadRole( final String role, final String implementation )
    {
        final Class<?> clazz;
        try
        {
            // check the role actually exists
            clazz = space.loadClass( role );
            if ( implementation.equals( role ) )
            {
                // also check constructor types
                clazz.getDeclaredConstructors();
            }
            return clazz;
        }
        catch ( final Throwable e )
        {
            // not all roles are needed, so just note for now
            logger.debug( "Missing Plexus role: " + role, e );
            return null;
        }
    }

    /**
     * Adds the given component implementation to the map, checking for potential duplicates.
     * 
     * @param component The Plexus component
     * @param implementation The component implementation
     * @return {@code true} if this is a new component; otherwise {@code false}
     */
    boolean addComponent( final Component component, final String implementation )
    {
        final DeferredClass<?> oldImplementation = components.get( component );
        if ( null != oldImplementation )
        {
            // check for simple component clashes and report conflicts
            if ( !oldImplementation.getName().equals( implementation ) )
            {
                logger.warn( "Duplicate implementations found for component key: " + component );
                logger.warn( "Saw: " + oldImplementation.getName() + " and: " + implementation );
            }
            return false;
        }

        components.put( component, space.deferLoadClass( implementation ) );
        return true;
    }

    /**
     * @return Plexus component map
     */
    Map<Component, DeferredClass<?>> getComponents()
    {
        return components;
    }
}
