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
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.objectweb.asm.ClassReader;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

/**
 * {@link PlexusBeanSource} that provides {@link PlexusBeanMetadata} based on runtime annotations.
 */
public final class AnnotatedPlexusBeanSource
    implements PlexusBeanSource, PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int CLASS_READER_FLAGS =
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<?, ?> variables;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Scans the given class space for components and annotation based Plexus metadata.
     * 
     * @param space The containing class space
     * @param variables The filter variables
     */
    public AnnotatedPlexusBeanSource( final ClassSpace space, final Map<?, ?> variables )
    {
        this.space = space;
        this.variables = variables;
    }

    /**
     * Provides runtime Plexus metadata based on property annotations, no scanning.
     * 
     * @param variables The filter variables
     */
    public AnnotatedPlexusBeanSource( final Map<?, ?> variables )
    {
        space = null;
        this.variables = variables;
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
            return Collections.emptyMap(); // nothing to scan
        }

        try
        {
            final PlexusComponentClassVisitor visitor = new PlexusComponentClassVisitor( space );
            final Enumeration<URL> e = space.findEntries( null, "*.class", true );
            while ( e.hasMoreElements() )
            {
                final InputStream in = e.nextElement().openStream();
                try
                {
                    new ClassReader( in ).accept( visitor, CLASS_READER_FLAGS );
                }
                finally
                {
                    IOUtil.close( in );
                }
            }
            return visitor.getComponents();
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
