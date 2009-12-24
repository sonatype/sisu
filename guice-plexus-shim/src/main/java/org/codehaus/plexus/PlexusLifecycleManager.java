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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Inject;

/**
 * {@link PlexusBeanManager} that manages Plexus components requiring lifecycle management.
 */
final class PlexusLifecycleManager
    implements PlexusBeanManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private Context context;

    @Inject( optional = true )
    LoggerManager loggerManager = new ConsoleLoggerManager();

    private final Map<String, String> descriptions = new HashMap<String, String>();

    private final Set<String> managedTypes = new HashSet<String>();

    private final List<Object> activeComponents = new ArrayList<Object>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getDescription( final String role, final String hint )
    {
        return descriptions.get( Roles.canonicalRoleHint( role, hint ) );
    }

    public boolean manage( final Component component )
    {
        final String key = Roles.canonicalRoleHint( component.role().getName(), component.hint() );
        return null == descriptions.put( key, component.description() );
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
                ( (LogEnabled) bean ).enableLogging( loggerManager.getLogger( name ) );
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
            loggerManager.getLogger( name ).error( "Problem starting: " + bean, e );
        }

        return true;
    }

    public void dispose()
    {
        while ( !activeComponents.isEmpty() )
        {
            final Object injectee = activeComponents.remove( 0 );
            final String name = injectee.getClass().getName();

            try
            {
                /*
                 * Run through the shutdown phase of the standard plexus "personality"
                 */
                if ( injectee instanceof Startable )
                {
                    ( (Startable) injectee ).stop();
                }
                if ( injectee instanceof Disposable )
                {
                    ( (Disposable) injectee ).dispose();
                }
            }
            catch ( final Throwable e )
            {
                loggerManager.getLogger( name ).error( "Problem stopping: " + injectee, e );
            }
            finally
            {
                loggerManager.returnLogger( name );
            }
        }
    }
}
