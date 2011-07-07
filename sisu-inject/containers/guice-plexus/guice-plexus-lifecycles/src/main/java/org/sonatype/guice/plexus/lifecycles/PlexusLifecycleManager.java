/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.lifecycles;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;

import com.google.inject.ProvisionException;

/**
 * {@link PlexusBeanManager} that manages Plexus components requiring lifecycle management.
 */
public final class PlexusLifecycleManager
    implements PlexusBeanManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Startable> startableBeans = new ArrayList<Startable>();

    private final List<Disposable> disposableBeans = new ArrayList<Disposable>();

    private final Logger consoleLogger = new ConsoleLogger();

    private final Provider<Context> plexusContextProvider;

    private final Provider<LoggerManager> plexusLoggerManagerProvider;

    private final Provider<?> slf4jLoggerFactoryProvider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusLifecycleManager( final Provider<Context> plexusContextProvider,
                                   final Provider<LoggerManager> plexusLoggerManagerProvider,
                                   final Provider<?> slf4jLoggerFactoryProvider )
    {
        this.plexusContextProvider = plexusContextProvider;
        this.plexusLoggerManagerProvider = plexusLoggerManagerProvider;
        this.slf4jLoggerFactoryProvider = slf4jLoggerFactoryProvider;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

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
                    property.set( bean, getSLF4JLogger( bean ) );
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

    public boolean manage( final Object bean )
    {
        if ( bean instanceof Disposable )
        {
            synchronized ( this )
            {
                disposableBeans.add( (Disposable) bean );
            }
        }
        if ( bean instanceof LogEnabled )
        {
            ( (LogEnabled) bean ).enableLogging( getPlexusLogger( bean ) );
        }
        if ( bean instanceof Contextualizable || bean instanceof Initializable || bean instanceof Startable )
        {
            manageLifecycle( bean );
        }
        return true;
    }

    public boolean unmanage( final Object bean )
    {
        synchronized ( this )
        {
            if ( startableBeans.remove( bean ) )
            {
                stop( (Startable) bean );
            }
            if ( disposableBeans.remove( bean ) )
            {
                dispose( (Disposable) bean );
            }
        }
        return true;
    }

    public boolean unmanage()
    {
        synchronized ( this )
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
        final String name = bean.getClass().getName();
        try
        {
            return plexusLoggerManagerProvider.get().getLoggerForComponent( name, null );
        }
        catch ( final Throwable e )
        {
            return consoleLogger;
        }
    }

    Object getSLF4JLogger( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
            return ( (org.slf4j.ILoggerFactory) slf4jLoggerFactoryProvider.get() ).getLogger( name );
        }
        catch ( final Throwable e )
        {
            return org.slf4j.LoggerFactory.getLogger( name );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void manageLifecycle( final Object bean )
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            for ( Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass() )
            {
                // need to check hierarchy in case bean is proxied
                final ClassLoader loader = clazz.getClassLoader();
                if ( loader instanceof URLClassLoader )
                {
                    Thread.currentThread().setContextClassLoader( loader );
                    break;
                }
            }
            /*
             * Run through the startup phase of the standard plexus "personality"
             */
            if ( bean instanceof Contextualizable )
            {
                contextualize( (Contextualizable) bean );
            }
            if ( bean instanceof Initializable )
            {
                initialize( (Initializable) bean );
            }
            if ( bean instanceof Startable )
            {
                synchronized ( this )
                {
                    // register before calling start in case it fails
                    final Startable startableBean = (Startable) bean;
                    startableBeans.add( startableBean );
                    start( startableBean );
                }
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    private void contextualize( final Contextualizable bean )
    {
        Logs.debug( "Contextualize: <>", bean, null );
        try
        {
            bean.contextualize( plexusContextProvider.get() );
        }
        catch ( final Throwable e )
        {
            final String message = "Error contextualizing: " + Logs.identityToString( bean );
            warn( message, e );
            if ( e instanceof RuntimeException )
            {
                throw (RuntimeException) e;
            }
            throw new ProvisionException( message, e );
        }
    }

    private void initialize( final Initializable bean )
    {
        Logs.debug( "Initialize: <>", bean, null );
        try
        {
            bean.initialize();
        }
        catch ( final Throwable e )
        {
            final String message = "Error initializing: " + Logs.identityToString( bean );
            warn( message, e );
            if ( e instanceof RuntimeException )
            {
                throw (RuntimeException) e;
            }
            throw new ProvisionException( message, e );
        }
    }

    private void start( final Startable bean )
    {
        Logs.debug( "Start: <>", bean, null );
        try
        {
            bean.start();
        }
        catch ( final Throwable e )
        {
            final String message = "Error starting: " + Logs.identityToString( bean );
            warn( message, e );
            if ( e instanceof RuntimeException )
            {
                throw (RuntimeException) e;
            }
            throw new ProvisionException( message, e );
        }
    }

    private void stop( final Startable bean )
    {
        Logs.debug( "Stop: <>", bean, null );
        try
        {
            bean.stop();
        }
        catch ( final Throwable e )
        {
            warn( "Problem stopping: " + Logs.identityToString( bean ), e );
        }
    }

    private void dispose( final Disposable bean )
    {
        Logs.debug( "Dispose: <>", bean, null );
        try
        {
            bean.dispose();
        }
        catch ( final Throwable e )
        {
            warn( "Problem disposing: " + Logs.identityToString( bean ), e );
        }
    }

    private void warn( final String message, final Throwable cause )
    {
        try
        {
            getPlexusLogger( this ).warn( message, cause );
        }
        catch ( final Throwable ignore )
        {
            System.err.println( message );
            cause.printStackTrace();
        }
    }
}
