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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

/**
 * {@link PlexusBeanSource} that collects {@link PlexusBeanMetadata} by scanning classes for runtime annotations.
 */
public final class AnnotatedPlexusBeanSource
    implements PlexusBeanSource, PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<?, ?> variables;

    private final PlexusComponentScanner scanner;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans the given class space for Plexus annotations using the given scanner.
     * 
     * @param space The containing class space
     * @param variables The filter variables
     * @param scanner The component scanner
     */
    public AnnotatedPlexusBeanSource( final ClassSpace space, final Map<?, ?> variables,
                                      final PlexusComponentScanner scanner )
    {
        this.space = space;
        this.variables = variables;
        this.scanner = scanner;
    }

    /**
     * Creates a bean source that scans the given class space for Plexus annotations using the default scanner.
     * 
     * @param space The containing class space
     * @param variables The filter variables
     */
    public AnnotatedPlexusBeanSource( final ClassSpace space, final Map<?, ?> variables )
    {
        this( space, variables, new AnnotatedPlexusComponentScanner() );
    }

    /**
     * Provides runtime Plexus metadata based on simple property annotations, no scanning.
     * 
     * @param variables The filter variables
     */
    public AnnotatedPlexusBeanSource( final Map<?, ?> variables )
    {
        this( null, variables, null );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean isEmpty()
    {
        return false; // metadata comes from the properties themselves
    }

    public Map<Component, DeferredClass<?>> findPlexusComponentBeans()
    {
        if ( null == space )
        {
            return Collections.emptyMap();
        }
        try
        {
            return scanner.scan( space, false );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e.toString() );
        }
    }

    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        return implementation.isAnnotationPresent( Component.class ) ? this : null;
    }

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        final Configuration configuration = property.getAnnotation( Configuration.class );
        if ( configuration != null && variables != null )
        {
            // support runtime interpolation of @Configuration values
            final String value = StringUtils.interpolate( configuration.value(), variables );
            if ( !value.equals( configuration.value() ) )
            {
                return new ConfigurationImpl( configuration.name(), value );
            }
        }
        return configuration;
    }

    public Requirement getRequirement( final BeanProperty<?> property )
    {
        return property.getAnnotation( Requirement.class );
    }
}
