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

import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;

/**
 * Runtime {@link PlexusBeanMetadata} based on {@link BeanProperty} annotations.
 */
public final class PlexusAnnotatedMetadata
    implements PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> variables;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Provides runtime Plexus metadata based on simple property annotations.
     * 
     * @param variables The filter variables
     */
    public PlexusAnnotatedMetadata( final Map<?, ?> variables )
    {
        this.variables = variables;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean isEmpty()
    {
        return false; // metadata comes from the properties themselves
    }

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        final Configuration configuration = property.getAnnotation( Configuration.class );
        if ( configuration != null && variables != null )
        {
            // support runtime interpolation of @Configuration values
            final String uninterpolatedValue = configuration.value();
            final String value = StringUtils.interpolate( uninterpolatedValue, variables );
            if ( !value.equals( uninterpolatedValue ) )
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
