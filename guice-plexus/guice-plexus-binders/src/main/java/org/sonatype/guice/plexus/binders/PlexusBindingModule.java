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

import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.inject.BeanListener;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.config.Strategies;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;
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

    private final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBindingModule( final PlexusBeanManager manager, final PlexusBeanSource... sources )
    {
        this.manager = manager;
        this.sources = sources.clone();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    protected void configure()
    {
        // attempt to register all known Plexus components
        for ( final PlexusBeanSource source : sources )
        {
            for ( final Entry<Component, DeferredClass<?>> e : source.findPlexusComponentBeans().entrySet() )
            {
                // should we register this component?
                final Component component = e.getKey();
                final DeferredClass<?> clazz = e.getValue();
                if ( null == manager || manager.manage( component, clazz ) )
                {
                    bindPlexusComponent( component, clazz );
                }
            }
        }

        // attach custom bean listener that performs injection of Plexus requirements/configuration
        bindListener( Matchers.any(), new BeanListener( new PlexusBeanBinder( manager, sources ) ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private void bindPlexusComponent( final Component component, final DeferredClass<?> clazz )
    {
        final Key roleKey = Roles.componentKey( component );
        final String strategy = component.instantiationStrategy();
        final ScopedBindingBuilder sbb;

        // simple case when role and implementation are identical
        if ( component.role().getName().equals( clazz.getName() ) )
        {
            if ( roleKey.getAnnotation() != null )
            {
                // wire annotated role to the plain role type
                sbb = bind( roleKey ).to( component.role() );
            }
            else
            {
                // simple wire to self
                sbb = bind( roleKey );
            }
        }
        else if ( Strategies.LOAD_ON_START.equals( strategy ) )
        {
            // no point deferring eager components
            sbb = bind( roleKey ).to( clazz.get() );
        }
        else
        {
            // mimic Plexus behaviour, only load component classes on demand
            sbb = bind( roleKey ).toProvider( new DeferredProvider( clazz ) );
        }

        if ( Strategies.LOAD_ON_START.equals( strategy ) )
        {
            sbb.asEagerSingleton();
        }
        else if ( !Strategies.PER_LOOKUP.equals( strategy ) )
        {
            // default Plexus policy
            sbb.in( Scopes.SINGLETON );
        }
    }
}
