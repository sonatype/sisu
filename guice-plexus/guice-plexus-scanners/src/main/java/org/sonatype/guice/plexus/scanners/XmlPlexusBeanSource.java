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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

/**
 * {@link PlexusBeanSource} that collects {@link PlexusBeanMetadata} by scanning XML resources.
 */
public final class XmlPlexusBeanSource
    implements PlexusBeanSource
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private Map<String, MappedPlexusBeanMetadata> metadata;

    private final PlexusComponentScanner scanner;

    private final boolean localSearch;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans all the surrounding class spaces for Plexus XML.
     * 
     * @param space The main class space
     * @param variables The filter variables
     * @param plexusXml The plexus.xml URL
     */
    public XmlPlexusBeanSource( final ClassSpace space, final Map<?, ?> variables, final URL plexusXml )
    {
        this.space = space;
        metadata = new HashMap<String, MappedPlexusBeanMetadata>();
        scanner = new XmlPlexusComponentScanner( variables, plexusXml, metadata );
        localSearch = false;
    }

    /**
     * Creates a bean source that scans the local class space for Plexus XML.
     * 
     * @param space The local class space
     * @param variables The filter variables
     */
    public XmlPlexusBeanSource( final ClassSpace space, final Map<?, ?> variables )
    {
        this.space = space;
        metadata = new HashMap<String, MappedPlexusBeanMetadata>();
        scanner = new XmlPlexusComponentScanner( variables, null, metadata );
        localSearch = true;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<Component, DeferredClass<?>> findPlexusComponentBeans()
    {
        try
        {
            return scanner.scan( space, localSearch );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e.toString() );
        }
    }

    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        final PlexusBeanMetadata beanMetadata = metadata.remove( implementation.getName() );
        if ( metadata.isEmpty() )
        {
            // avoid leaving sparse maps around
            metadata = Collections.emptyMap();
        }
        return beanMetadata;
    }
}
