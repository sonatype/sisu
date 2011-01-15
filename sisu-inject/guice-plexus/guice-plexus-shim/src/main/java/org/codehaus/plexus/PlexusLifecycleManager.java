package org.codehaus.plexus;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.sonatype.guice.bean.inject.BeanListener;
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

    private static final ThreadLocal<List<Object>> lifecycleBeans = new ThreadLocal<List<Object>>()
    {
        @Override
        protected List<Object> initialValue()
        {
            return new ArrayList<Object>( 4 );
        }
    };

    private final List<Startable> startableBeans = Collections.synchronizedList( new ArrayList<Startable>() );

    private final List<Disposable> disposableBeans = Collections.synchronizedList( new ArrayList<Disposable>() );

    private final AtomicInteger numDeferredBeans = new AtomicInteger();

    private final MutablePlexusContainer container;

    private final Context context;

    List<org.slf4j.ILoggerFactory> loggerFactories;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusLifecycleManager( final MutablePlexusContainer container, final Context context )
    {
        this.container = container;
        this.context = context;

        try
        {
            loggerFactories = container.lookupList( org.slf4j.ILoggerFactory.class );
        }
        catch ( final Throwable e )
        {
            loggerFactories = Collections.emptyList();
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean manage( final Class<?> clazz )
    {
        return true;
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
                    final String name = bean.getClass().getName();
                    final Iterator<org.slf4j.ILoggerFactory> itr = loggerFactories.iterator();
                    if ( itr.hasNext() )
                    {
                        property.set( bean, itr.next().getLogger( name ) );
                    }
                    else
                    {
                        property.set( bean, org.slf4j.LoggerFactory.getILoggerFactory().getLogger( name ) );
                    }
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
            disposableBeans.add( (Disposable) bean );
        }
        if ( bean instanceof LogEnabled )
        {
            ( (LogEnabled) bean ).enableLogging( getPlexusLogger( bean ) );
        }
        if ( numDeferredBeans.get() > 0 && !BeanListener.isInjecting() )
        {
            manageDeferredLifecycles();
        }
        if ( bean instanceof Contextualizable || bean instanceof Initializable || bean instanceof Startable )
        {
            if ( BeanListener.isInjecting() )
            {
                deferLifecycle( bean );
            }
            else
            {
                manageLifecycle( bean );
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

    private void deferLifecycle( final Object bean )
    {
        lifecycleBeans.get().add( bean );
        numDeferredBeans.incrementAndGet();
    }

    private void manageDeferredLifecycles()
    {
        final List<Object> beans = new ArrayList<Object>( lifecycleBeans.get() );
        numDeferredBeans.addAndGet( -beans.size() );
        lifecycleBeans.remove();

        for ( final Object bean : beans )
        {
            manageLifecycle( bean );
        }
    }

    private void manageLifecycle( final Object bean )
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
                // register before calling start in case it fails
                final Startable startableBean = (Startable) bean;
                startableBeans.add( startableBean );
                start( startableBean );
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    private void contextualize( final Contextualizable bean )
    {
        try
        {
            bean.contextualize( context );
        }
        catch ( final Throwable e )
        {
            final String message = "Error contextualizing: " + bean.getClass();
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
        try
        {
            bean.initialize();
        }
        catch ( final Throwable e )
        {
            final String message = "Error initializing: " + bean.getClass();
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
        try
        {
            bean.start();
        }
        catch ( final Throwable e )
        {
            final String message = "Error starting: " + bean.getClass();
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
