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
package org.codehaus.plexus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.config.Roles;

/**
 * {@link PlexusBeanManager} that manages Plexus components requiring lifecycle management.
 */
final class PlexusLifecycleManager
    implements PlexusBeanManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, DeferredClass<?>> implementations = new HashMap<String, DeferredClass<?>>();

    private final Map<String, String> descriptions = new HashMap<String, String>();

    private final Set<String> managedTypes = new HashSet<String>();

    private final List<Object> activeComponents = new ArrayList<Object>();

    @Inject
    private Context context;

    @Inject
    private PlexusContainer container;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean manage( final Component component, final DeferredClass<?> clazz )
    {
        final String key = Roles.canonicalRoleHint( component );
        if ( implementations.containsKey( key ) )
        {
            return false; // component already exists
        }
        implementations.put( key, clazz );
        final String description = component.description();
        if ( description.length() > 0 )
        {
            descriptions.put( key, description );
        }
        return true;
    }

    public boolean manage( final Class<?> clazz )
    {
        if ( LogEnabled.class.isAssignableFrom( clazz ) || Contextualizable.class.isAssignableFrom( clazz )
            || Initializable.class.isAssignableFrom( clazz ) || Startable.class.isAssignableFrom( clazz )
            || Disposable.class.isAssignableFrom( clazz ) )
        {
            return managedTypes.add( clazz.getName() );
        }
        return false;
    }

    public boolean manage( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
            /*
             * Run through the startup phase of the standard plexus "personality"
             */
            if ( bean instanceof LogEnabled )
            {
                ( (LogEnabled) bean ).enableLogging( getLogger( name ) );
            }
            if ( bean instanceof Contextualizable )
            {
                ( (Contextualizable) bean ).contextualize( context );
            }
            if ( bean instanceof Initializable )
            {
                ( (Initializable) bean ).initialize();
            }
            if ( bean instanceof Startable )
            {
                ( (Startable) bean ).start();
                activeComponents.add( bean );
            }
            else if ( bean instanceof Disposable )
            {
                activeComponents.add( bean );
            }
        }
        catch ( final Exception e )
        {
            getLogger( name ).error( "Problem starting: " + bean, e );
        }

        return true;
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final String role, final String hint )
    {
        final String key = Roles.canonicalRoleHint( role, hint );

        @SuppressWarnings( "unchecked" )
        final DeferredClass clazz = implementations.get( key );
        if ( null == clazz )
        {
            return null;
        }

        final ComponentDescriptor<T> descriptor = new ComponentDescriptor<T>();
        descriptor.setRole( role );
        descriptor.setRoleHint( hint );
        descriptor.setImplementationClass( clazz.get() );
        descriptor.setDescription( descriptions.get( key ) );
        return descriptor;
    }

    // ----------------------------------------------------------------------
    // Shared implementation methods
    // ----------------------------------------------------------------------

    void dispose( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
            activeComponents.remove( bean );

            /*
             * Run through the shutdown phase of the standard plexus "personality"
             */
            if ( bean instanceof Startable )
            {
                ( (Startable) bean ).stop();
            }
            if ( bean instanceof Disposable )
            {
                ( (Disposable) bean ).dispose();
            }
        }
        catch ( final Throwable e )
        {
            getLogger( name ).error( "Problem stopping: " + bean, e );
        }
        finally
        {
            releaseLogger( name );
        }
    }

    void dispose()
    {
        while ( !activeComponents.isEmpty() )
        {
            dispose( activeComponents.get( 0 ) );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Logger getLogger( final String name )
    {
        return container.getLoggerManager().getLoggerForComponent( name, null );
    }

    private void releaseLogger( final String name )
    {
        container.getLoggerManager().returnComponentLogger( name, null );
    }
}
