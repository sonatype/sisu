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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.config.Strategies;

/**
 * Enhanced Plexus component map with additional book-keeping.
 */
final class PlexusComponentRegistry
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Component LOAD_ON_START_PLACEHOLDER =
        new ComponentImpl( Object.class, "", Strategies.LOAD_ON_START, "" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger( PlexusComponentRegistry.class );

    private final Map<String, Component> components = new HashMap<String, Component>();

    private final Map<String, DeferredClass<?>> implementations = new HashMap<String, DeferredClass<?>>();

    private final Set<String> deferredNames = new HashSet<String>();

    private final ClassSpace space;

    private ClassSpace disambiguatedSpace;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusComponentRegistry( final ClassSpace space )
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
     * Records that the given Plexus component should be loaded when the container starts.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     */
    void loadOnStart( final String role, final String hint )
    {
        final String key = Roles.canonicalRoleHint( role, hint );
        final Component c = components.get( key );
        if ( null == c )
        {
            components.put( key, LOAD_ON_START_PLACEHOLDER );
        }
        else if ( !Strategies.LOAD_ON_START.equals( c.instantiationStrategy() ) )
        {
            components.put( key, new ComponentImpl( c.role(), c.hint(), Strategies.LOAD_ON_START, c.description() ) );
        }
    }

    /**
     * Registers the given component, automatically disambiguating between implementations bound multiple times.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @param instantiationStrategy The instantiation strategy
     * @param description The component description
     * @param implementation The implementation
     * @return The implementation the component was successfully registered with; otherwise {@code null}
     */
    String addComponent( final String role, final String hint, final String instantiationStrategy,
                         final String description, final String implementation )
    {
        final Class<?> clazz = loadRole( role, implementation );
        if ( null == clazz )
        {
            return null;
        }

        final String canonicalHint = Hints.canonicalHint( hint );
        final String key = Roles.canonicalRoleHint( role, canonicalHint );

        /*
         * COMPONENT...
         */
        final Component oldComponent = components.get( key );
        if ( null == oldComponent )
        {
            components.put( key, new ComponentImpl( clazz, canonicalHint, instantiationStrategy, description ) );
        }
        else if ( LOAD_ON_START_PLACEHOLDER == oldComponent )
        {
            components.put( key, new ComponentImpl( clazz, canonicalHint, Strategies.LOAD_ON_START, description ) );
        }

        /*
         * ...IMPLEMENTATION
         */
        final DeferredClass<?> oldImplementation = implementations.get( key );
        if ( null == oldImplementation )
        {
            final DeferredClass<?> newImplementation;
            if ( deferredNames.add( implementation ) )
            {
                newImplementation = space.deferLoadClass( implementation );
            }
            else
            {
                if ( null == disambiguatedSpace )
                {
                    disambiguatedSpace = new DisambiguatedClassSpace( space );
                }
                newImplementation = disambiguatedSpace.deferLoadClass( implementation );
            }
            implementations.put( key, newImplementation );
            return newImplementation.getName();
        }
        else if ( oldImplementation.getName().equals( implementation ) )
        {
            return implementation; // merge configuration
        }

        logger.warn( "Duplicate implementations found for Plexus component " + key );
        logger.warn( "Saw: " + oldImplementation.getName() + " and: " + implementation );

        return null;
    }

    /**
     * @return Plexus component map
     */
    Map<Component, DeferredClass<?>> getComponents()
    {
        final Map<Component, DeferredClass<?>> map = new HashMap<Component, DeferredClass<?>>();
        for ( final Entry<String, DeferredClass<?>> i : implementations.entrySet() )
        {
            map.put( components.get( i.getKey() ), i.getValue() );
        }
        return map;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to load the given Plexus role, checks constructors for concrete types.
     * 
     * @param role The Plexus role
     * @param implementation The implementation
     * @return Loaded Plexus role
     */
    private Class<?> loadRole( final String role, final String implementation )
    {
        try
        {
            final Class<?> clazz = space.loadClass( role );
            if ( implementation.equals( role ) )
            {
                // check constructors will load
                clazz.getDeclaredConstructors();
            }
            return clazz;
        }
        catch ( final Throwable e )
        {
            // not all roles are needed, so just note those we couldn't load
            logger.debug( "Ignoring Plexus role: " + role + " [" + e + "]" );
            return null;
        }
    }
}
