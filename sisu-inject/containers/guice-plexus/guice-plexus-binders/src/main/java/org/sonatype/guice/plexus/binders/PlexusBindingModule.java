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
package org.sonatype.guice.plexus.binders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sonatype.guice.bean.inject.BeanListener;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

/**
 * Guice {@link Module} that supports registration, injection, and management of Plexus beans.
 */
public final class PlexusBindingModule
    extends AbstractModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PlexusBeanManager manager;

    private final PlexusBeanModule[] modules;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBindingModule( final PlexusBeanManager manager, final PlexusBeanModule... modules )
    {
        this.manager = manager;
        this.modules = modules.clone();
    }

    public PlexusBindingModule( final PlexusBeanManager manager, final Collection<PlexusBeanModule> modules )
    {
        this.manager = manager;
        this.modules = modules.toArray( new PlexusBeanModule[modules.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    protected void configure()
    {
        final List<PlexusBeanSource> sources = new ArrayList<PlexusBeanSource>( modules.length );
        for ( final PlexusBeanModule module : modules )
        {
            final PlexusBeanSource source = module.configure( binder() );
            if ( null != source )
            {
                sources.add( source );
            }
        }

        // attach custom bean listener to perform injection of Plexus requirements/configuration
        bindListener( Matchers.any(), new BeanListener( new PlexusBeanBinder( manager, sources ) ) );
    }
}
