package org.sonatype.guice.plexus.binders;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.scanners.PlexusAnnotatedMetadata;
import org.sonatype.guice.plexus.scanners.PlexusTypeVisitor;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

/**
 * {@link PlexusBeanModule} that registers Plexus beans by scanning classes for runtime annotations.
 */
public final class PlexusAnnotatedBeanModule
    implements PlexusBeanModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Map<String, List<Element>> CACHED_ELEMENTS = new HashMap<String, List<Element>>();

    private final ClassSpace space;

    private final Map<?, ?> variables;

    private final boolean caching;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans the given class space for Plexus annotations using the given scanner.
     * 
     * @param space The local class space
     * @param variables The filter variables
     */
    public PlexusAnnotatedBeanModule( final ClassSpace space, final Map<?, ?> variables )
    {
        this( space, variables, false );
    }

    /**
     * Creates a bean source that scans the given class space for Plexus annotations using the given scanner.
     * 
     * @param space The local class space
     * @param variables The filter variables
     * @param caching Cache the results?
     */
    public PlexusAnnotatedBeanModule( final ClassSpace space, final Map<?, ?> variables, final boolean caching )
    {
        this.space = space;
        this.variables = variables;
        this.caching = caching;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public PlexusBeanSource configure( final Binder binder )
    {
        if ( caching )
        {
            cachingScan( binder );
        }
        else
        {
            scan( binder );
        }
        return new PlexusAnnotatedBeanSource( variables );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    void scan( final Binder binder )
    {
        if ( null != space )
        {
            new ClassSpaceScanner( space ).accept( new PlexusTypeVisitor( new PlexusTypeBinder( binder ) ) );
        }
    }

    private void cachingScan( final Binder binder )
    {
        synchronized ( CACHED_ELEMENTS )
        {
            final String key = String.valueOf( space );
            if ( !CACHED_ELEMENTS.containsKey( key ) )
            {
                CACHED_ELEMENTS.put( key, Elements.getElements( new RecordingModule() ) );
            }
            for ( final Element e : CACHED_ELEMENTS.get( key ) )
            {
                e.applyTo( binder );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    final class RecordingModule
        implements Module
    {
        public void configure( final Binder binder )
        {
            scan( binder );
        }
    }

    private static final class PlexusAnnotatedBeanSource
        implements PlexusBeanSource
    {
        private final PlexusBeanMetadata metadata;

        PlexusAnnotatedBeanSource( final Map<?, ?> variables )
        {
            metadata = new PlexusAnnotatedMetadata( variables );
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            return implementation.isAnnotationPresent( Component.class ) ? metadata : null;
        }
    }
}
