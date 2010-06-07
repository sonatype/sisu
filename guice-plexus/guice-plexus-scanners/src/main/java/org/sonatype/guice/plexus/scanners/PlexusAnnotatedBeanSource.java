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
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

/**
 * {@link PlexusBeanSource} that collects {@link PlexusBeanMetadata} by scanning classes for runtime annotations.
 */
public class PlexusAnnotatedBeanSource
    implements PlexusBeanSource
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final PlexusAnnotatedMetadata metadata;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans the given class space for Plexus annotations using the given scanner.
     * 
     * @param space The local class space
     * @param variables The filter variables
     */
    public PlexusAnnotatedBeanSource( final ClassSpace space, final Map<?, ?> variables )
    {
        this.space = space;

        metadata = new PlexusAnnotatedMetadata( variables );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final Map<Component, DeferredClass<?>> findPlexusComponentBeans()
    {
        if ( null != space )
        {
            final PlexusTypeVisitor visitor = new PlexusTypeVisitor( new PlexusTypeRegistry( space ) );
            new ClassSpaceScanner( space ).accept( visitor );
            return visitor.getComponents();
        }
        return Collections.emptyMap();
    }

    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        return implementation.isAnnotationPresent( Component.class ) ? metadata : null;
    }
}
