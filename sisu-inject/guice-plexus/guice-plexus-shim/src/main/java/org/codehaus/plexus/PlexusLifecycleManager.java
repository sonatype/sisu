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

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;

import com.google.inject.ProvisionException;

/**
 * {@link PlexusBeanManager} that manages Plexus components requiring lifecycle management.
 */
final class PlexusLifecycleManager
    implements PlexusBeanManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Startable> startableBeans = new ArrayList<Startable>();

    private final List<Disposable> disposableBeans = new ArrayList<Disposable>();

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

    public boolean manage( final Class<?> clazz )
    {
        return LogEnabled.class.isAssignableFrom( clazz ) || Contextualizable.class.isAssignableFrom( clazz )
            || Initializable.class.isAssignableFrom( clazz ) || Startable.class.isAssignableFrom( clazz )
            || Disposable.class.isAssignableFrom( clazz );
    }

    @SuppressWarnings( "rawtypes" )
    public PropertyBinding manage( final BeanProperty property )
    {
        final Class clazz = property.getType().getRawType();
        if ( "org.slf4j.Logger".equals( clazz.getName() ) )
        {
            return new PropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
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
                @SuppressWarnings( "unchecked" )
                public <B> void injectProperty( final B bean )
                {
                    property.set( bean, getPlexusLogger( bean ) );
                }
            };
        }
        return null;
    }

    public synchronized boolean manage( final Object bean )
    {
        final boolean isStartable = bean instanceof Startable;
        final boolean isContextualizable = bean instanceof Contextualizable;
        final boolean isInitializable = bean instanceof Initializable;
        final boolean isDisposable = bean instanceof Disposable;

        /*
         * Record this information before starting, in case there's a problem
         */
        if ( isStartable )
        {
            startableBeans.add( (Startable) bean );
        }
        if ( isDisposable )
        {
            disposableBeans.add( (Disposable) bean );
        }

        if ( bean instanceof LogEnabled )
        {
            ( (LogEnabled) bean ).enableLogging( getPlexusLogger( bean ) );
        }

        /*
         * Run through the startup phase of the standard plexus "personality"
         */
        if ( isContextualizable || isInitializable || isStartable )
        {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try
            {
                for ( Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass() )
                {
                    // need to check hierarchy in case bean is proxied
                    final ClassLoader loader = clazz.getClassLoader();
                    if ( loader instanceof ClassRealm )
                    {
                        Thread.currentThread().setContextClassLoader( loader );
                        break;
                    }
                }
                if ( isContextualizable )
                {
                    contextualize( (Contextualizable) bean );
                }
                if ( isInitializable )
                {
                    initialize( (Initializable) bean );
                }
                if ( isStartable )
                {
                    start( (Startable) bean );
                }
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( tccl );
            }
        }
        return true;
    }

    public synchronized boolean unmanage( final Object bean )
    {
        if ( startableBeans.remove( bean ) )
        {
            stop( (Startable) bean );
        }
        if ( disposableBeans.remove( bean ) )
        {
            dispose( (Disposable) bean );
        }
        return true;
    }

    public synchronized boolean unmanage()
    {
        // unmanage beans in reverse order
        while ( !startableBeans.isEmpty() )
        {
            stop( startableBeans.remove( startableBeans.size() - 1 ) );
        }
        while ( !disposableBeans.isEmpty() )
        {
            dispose( disposableBeans.remove( disposableBeans.size() - 1 ) );
        }
        return true;
    }

    public PlexusBeanManager manageChild()
    {
        return this;
    }

    // ----------------------------------------------------------------------
    // Shared implementation methods
    // ----------------------------------------------------------------------

    Logger getPlexusLogger( final Object bean )
    {
        return container.getLoggerManager().getLoggerForComponent( bean.getClass().getName(), null );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void contextualize( final Contextualizable bean )
    {
        try
        {
            bean.contextualize( context );
        }
        catch ( final ContextException e )
        {
            throw new ProvisionException( "Error contextualizing: " + bean.getClass(), e );
        }
    }

    private static void initialize( final Initializable bean )
    {
        try
        {
            bean.initialize();
        }
        catch ( final InitializationException e )
        {
            throw new ProvisionException( "Error initializing: " + bean.getClass(), e );
        }
    }

    private static void start( final Startable bean )
    {
        try
        {
            bean.start();
        }
        catch ( final StartingException e )
        {
            throw new ProvisionException( "Error starting: " + bean.getClass(), e );
        }
    }

    private void stop( final Startable bean )
    {
        try
        {
            bean.stop();
        }
        catch ( final Throwable e )
        {
            warn( "Problem stopping: " + bean.getClass(), e );
        }
    }

    private void dispose( final Disposable bean )
    {
        try
        {
            bean.dispose();
        }
        catch ( final Throwable e )
        {
            warn( "Problem disposing: " + bean.getClass(), e );
        }
    }

    private void warn( final String message, final Throwable cause )
    {
        try
        {
            container.getLogger().warn( message, cause );
        }
        catch ( final Throwable ignore )
        {
            System.err.println( message );
            cause.printStackTrace();
        }
    }
}
