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

import java.lang.annotation.Annotation;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.binders.QualifiedTypeBinder;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.guice.plexus.config.PlexusBeanDescription;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.guice.plexus.scanners.PlexusTypeListener;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;

public final class PlexusTypeBinder
    implements PlexusTypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder binder;

    private final QualifiedTypeListener qualifiedTypeBinder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusTypeBinder( final Binder binder )
    {
        this.binder = binder;

        qualifiedTypeBinder = new QualifiedTypeBinder( binder );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        qualifiedTypeBinder.hear( qualifier, qualifiedType, source );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void hear( final Component component, final DeferredClass<?> clazz, final Object source )
    {
        final Key roleKey = Roles.componentKey( component );
        final String strategy = component.instantiationStrategy();
        final Class<?> role = component.role();
        final ScopedBindingBuilder sbb;

        final Binder componentBinder = componentBinder( source, component.description() );

        // special case when role is the implementation
        if ( role.getName().equals( clazz.getName() ) )
        {
            if ( roleKey.getAnnotation() != null )
            {
                sbb = componentBinder.bind( roleKey ).to( role );
            }
            else
            {
                sbb = componentBinder.bind( roleKey );
            }
        }
        else if ( Strategies.LOAD_ON_START.equals( strategy ) || clazz instanceof LoadedClass<?> )
        {
            sbb = componentBinder.bind( roleKey ).to( clazz.load() ); // no need to defer
        }
        else
        {
            sbb = componentBinder.bind( roleKey ).toProvider( clazz.asProvider() );
        }

        if ( Strategies.LOAD_ON_START.equals( strategy ) )
        {
            sbb.asEagerSingleton();
        }
        else if ( !Strategies.PER_LOOKUP.equals( strategy ) )
        {
            sbb.in( Scopes.SINGLETON );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Binder componentBinder( final Object source, final String description )
    {
        if ( null != description && description.length() > 0 )
        {
            return binder.withSource( new DefaultPlexusBeanDescription( source, description ) );
        }
        return binder.withSource( source );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class DefaultPlexusBeanDescription
        implements PlexusBeanDescription
    {
        private final Object source;

        private final String description;

        DefaultPlexusBeanDescription( final Object source, final String description )
        {
            this.source = source;
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public String toString()
        {
            return source.toString();
        }
    }
}
