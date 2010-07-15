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
