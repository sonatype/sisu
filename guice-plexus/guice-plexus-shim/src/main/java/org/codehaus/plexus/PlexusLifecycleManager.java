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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;

/**
 * {@link PlexusBeanManager} that manages Plexus components requiring lifecycle management.
 */
final class PlexusLifecycleManager
    implements PlexusBeanManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<DeferredClass<?>, String> descriptions = new HashMap<DeferredClass<?>, String>();

    private final List<Object> activeBeans = Collections.synchronizedList( new ArrayList<Object>() );

    private final PlexusContainer container;

    private final Context context;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusLifecycleManager( final PlexusContainer container, final Context context )
    {
        this.container = container;
        this.context = context;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean manage( final Component component, final DeferredClass<?> clazz )
    {
        final String description = component.description();
        if ( null != description && description.length() > 0 )
        {
            descriptions.put( clazz, description );
        }
        return true;
    }

    public boolean manage( final Class<?> clazz )
    {
        return LogEnabled.class.isAssignableFrom( clazz ) || Contextualizable.class.isAssignableFrom( clazz )
            || Initializable.class.isAssignableFrom( clazz ) || Startable.class.isAssignableFrom( clazz )
            || Disposable.class.isAssignableFrom( clazz );
    }

    @SuppressWarnings( "unchecked" )
    public PropertyBinding manage( final BeanProperty property )
    {
        final Class clazz = property.getType().getRawType();
        if ( "org.slf4j.Logger".equals( clazz.getName() ) )
        {
            return new PropertyBinding()
            {
                public <B> void injectProperty( final B bean )
                {
                    property.set( bean, org.slf4j.LoggerFactory.getLogger( bean.getClass() ) );
                }
            };
        }
        if ( Logger.class.equals( clazz ) )
        {
            return new PropertyBinding()
            {
                public <B> void injectProperty( final B bean )
                {
                    property.set( bean, getLogger( bean.getClass().getName() ) );
                }
            };
        }
        return null;
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
                activeBeans.add( bean );
            }
            else if ( bean instanceof Disposable )
            {
                activeBeans.add( bean );
            }
        }
        catch ( final Throwable e )
        {
            getLogger( name ).error( "Problem starting: " + bean, e );
        }

        return true;
    }

    public boolean unmanage( final Object bean )
    {
        return activeBeans.remove( bean );
    }

    public synchronized boolean unmanage()
    {
        boolean result = false;
        while ( !activeBeans.isEmpty() )
        {
            // dispose in reverse order; use index to allow concurrent growth
            result |= dispose( activeBeans.remove( activeBeans.size() - 1 ) );
        }
        return result;
    }

    public PlexusBeanManager manageChild()
    {
        return this;
    }

    // ----------------------------------------------------------------------
    // Shared implementation methods
    // ----------------------------------------------------------------------

    String getDescription( final DeferredClass<?> clazz )
    {
        final String description = descriptions.get( clazz );
        if ( null != description )
        {
            return description;
        }
        if ( clazz instanceof LoadedClass<?> )
        {
            // can't find an exact match, need to compare using name and type equality
            for ( final Entry<DeferredClass<?>, String> e : descriptions.entrySet() )
            {
                final DeferredClass<?> key = e.getKey();
                if ( clazz.getName().equals( key.getName() ) && clazz.load().equals( key.load() ) )
                {
                    return e.getValue();
                }
            }
        }
        return null;
    }

    Logger getLogger( final String name )
    {
        return container.getLoggerManager().getLoggerForComponent( name, null );
    }

    void releaseLogger( final String name )
    {
        container.getLoggerManager().returnComponentLogger( name, null );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private boolean dispose( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
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
        return true;
    }
}
