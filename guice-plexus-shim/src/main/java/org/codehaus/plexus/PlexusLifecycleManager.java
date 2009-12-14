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
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.sonatype.guice.plexus.binders.BeanWatcher;

import com.google.inject.matcher.AbstractMatcher;

/**
 * {@link BeanWatcher} that watches for Plexus components requiring lifecycle management.
 */
final class PlexusLifecycleManager
    extends AbstractMatcher<Class<?>>
    implements BeanWatcher
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Object> activeComponents = new ArrayList<Object>();

    private final DefaultPlexusContainer container;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusLifecycleManager( final DefaultPlexusContainer container )
    {
        this.container = container;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean matches( final Class<?> clazz )
    {
        return LogEnabled.class.isAssignableFrom( clazz ) || Contextualizable.class.isAssignableFrom( clazz )
            || Initializable.class.isAssignableFrom( clazz ) || Startable.class.isAssignableFrom( clazz )
            || Disposable.class.isAssignableFrom( clazz );
    }

    public void afterInjection( final Object injectee )
    {
        final String name = injectee.getClass().getName();

        try
        {
            /*
             * Run through the startup phase of the standard plexus "personality"
             */
            if ( injectee instanceof LogEnabled )
            {
                ( (LogEnabled) injectee ).enableLogging( container.loggerManager.getLogger( name ) );
            }
            if ( injectee instanceof Contextualizable )
            {
                ( (Contextualizable) injectee ).contextualize( container.context );
            }
            if ( injectee instanceof Initializable )
            {
                ( (Initializable) injectee ).initialize();
            }
            if ( injectee instanceof Startable )
            {
                ( (Startable) injectee ).start();
                activeComponents.add( injectee );
            }
            else if ( injectee instanceof Disposable )
            {
                activeComponents.add( injectee );
            }
        }
        catch ( final Exception e )
        {
            container.loggerManager.getLogger( name ).error( "Problem starting: " + injectee, e );
        }
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
                container.loggerManager.getLogger( name ).error( "Problem stopping: " + injectee, e );
            }
            finally
            {
                container.loggerManager.returnLogger( name );
            }
        }
    }
}
