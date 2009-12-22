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
import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.BeanListener;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice {@link Module} that registers a custom {@link TypeListener} to auto-bind Plexus beans.
 */
public final class PlexusBindingModule
    extends AbstractModule
    implements BeanBinder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanWatcher beanWatcher;

    private final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a Guice {@link Module} that auto-binds Plexus beans according to the given metadata sources.
     * 
     * @param beanWatcher An optional bean watcher
     * @param sources The Plexus metadata sources
     */
    public PlexusBindingModule( final BeanWatcher beanWatcher, final PlexusBeanSource... sources )
    {
        this.beanWatcher = beanWatcher;
        this.sources = sources.clone();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    protected void configure()
    {
        for ( final PlexusBeanSource source : sources )
        {
            for ( final Entry<Component, DeferredClass<?>> e : source.findPlexusComponentBeans().entrySet() )
            {
                registerPlexusBean( e.getKey(), e.getValue() );
            }
        }

        // wire Plexus requirements/configurations into beans
        bindListener( Matchers.any(), new BeanListener( this ) );
    }

    public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        final Class<?> clazz = type.getRawType();

        // watchers are a way to mix-in behaviour, like lifecycles
        if ( null != beanWatcher && beanWatcher.matches( clazz ) )
        {
            encounter.register( beanWatcher );
        }

        for ( final PlexusBeanSource source : sources )
        {
            // use first source that has metadata for the given implementation
            final PlexusBeanMetadata metadata = source.getBeanMetadata( clazz );
            if ( metadata != null )
            {
                return new PlexusPropertyBinder( encounter, metadata );
            }
        }

        return null; // no need to auto-bind
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private void registerPlexusBean( final Component component, final DeferredClass<?> clazz )
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
                // no wiring required
                sbb = bind( roleKey );
            }
        }
        else if ( "load-on-start".equals( strategy ) )
        {
            // no point deferring eager components
            sbb = bind( roleKey ).to( clazz.get() );
        }
        else
        {
            // mimic Plexus behaviour, only load component classes on demand
            sbb = bind( roleKey ).toProvider( new DeferredProvider( clazz ) );
        }

        if ( "load-on-start".equals( strategy ) )
        {
            sbb.asEagerSingleton();
        }
        else if ( !"per-lookup".equals( strategy ) )
        {
            // default Plexus policy
            sbb.in( Scopes.SINGLETON );
        }
    }
}
