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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.scanners.PlexusXmlScanner;

import com.google.inject.Binder;

/**
 * {@link PlexusBeanSource} that collects {@link PlexusBeanMetadata} by scanning XML resources.
 */
public final class PlexusXmlBeanSource
    implements PlexusBeanSource
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<?, ?> variables;

    private final URL plexusXml;

    private final boolean localSearch;

    private Map<String, PlexusBeanMetadata> metadata = new HashMap<String, PlexusBeanMetadata>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans all the surrounding class spaces for XML resources.
     * 
     * @param space The main class space
     * @param variables The filter variables
     * @param plexusXml The plexus.xml URL
     */
    public PlexusXmlBeanSource( final ClassSpace space, final Map<?, ?> variables, final URL plexusXml )
    {
        this.space = space;
        this.variables = variables;
        this.plexusXml = plexusXml;
        localSearch = false;
    }

    /**
     * Creates a bean source that only scans the local class space for XML resources.
     * 
     * @param space The local class space
     * @param variables The filter variables
     */
    public PlexusXmlBeanSource( final ClassSpace space, final Map<?, ?> variables )
    {
        this.space = space;
        this.variables = variables;
        plexusXml = null;
        localSearch = true;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final PlexusTypeBinder plexusTypeBinder = new PlexusTypeBinder( binder );
        try
        {
            final PlexusXmlScanner scanner = new PlexusXmlScanner( variables, plexusXml, metadata );
            final Map<Component, DeferredClass<?>> components = scanner.scan( space, localSearch );
            for ( final Entry<Component, DeferredClass<?>> entry : components.entrySet() )
            {
                plexusTypeBinder.hear( entry.getKey(), entry.getValue(), space );
            }
        }
        catch ( final IOException e )
        {
            binder.addError( e );
        }
    }

    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        final PlexusBeanMetadata beanMetadata = metadata.remove( implementation.getName() );
        if ( null != beanMetadata && metadata.isEmpty() )
        {
            // avoid leaving sparse maps around
            metadata = Collections.emptyMap();
        }
        return beanMetadata;
    }
}
