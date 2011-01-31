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

import java.util.List;

import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanBinder} that binds bean properties according to Plexus metadata.
 */
final class PlexusBeanBinder
    implements BeanBinder, InjectionListener<Object>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PlexusBeanManager manager;

    private final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusBeanBinder( final PlexusBeanManager manager, final List<PlexusBeanSource> sources )
    {
        this.manager = manager;
        this.sources = sources.toArray( new PlexusBeanSource[sources.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        // do we need to report bean instances?
        final Class<?> clazz = type.getRawType();
        if ( null != manager && manager.manage( clazz ) )
        {
            encounter.register( this );
        }

        for ( final PlexusBeanSource source : sources )
        {
            // use first source that has metadata for the given implementation
            final PlexusBeanMetadata metadata = source.getBeanMetadata( clazz );
            if ( metadata != null )
            {
                return new PlexusPropertyBinder( manager, encounter, metadata );
            }
        }

        return null; // no need to auto-bind
    }

    public void afterInjection( final Object bean )
    {
        // report bean creation
        manager.manage( bean );
    }
}
