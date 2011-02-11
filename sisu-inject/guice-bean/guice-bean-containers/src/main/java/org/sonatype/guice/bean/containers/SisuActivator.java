/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.containers;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.BundleClassSpace;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.inject.BeanScanning;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Providers;

/**
 * {@link BundleActivator} that maintains a dynamic {@link Injector} graph by scanning bundles as they come and go.
 */
public final class SisuActivator
    implements BundleActivator, BundleTrackerCustomizer, ServiceTrackerCustomizer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String BUNDLE_INJECTOR_CLASS_NAME = BundleInjector.class.getName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    static final MutableBeanLocator locator = new DefaultBeanLocator();

    static final Injector defaultInjector = Guice.createInjector( new Module()
    {
        public void configure( final Binder binder )
        {
            binder.bind( BeanLocator.class ).toProvider( Providers.of( locator ) );
        }
    } );

    private BundleContext bundleContext;

    private ServiceTracker serviceTracker;

    private BundleTracker bundleTracker;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final BundleContext context )
    {
        SisuContainer.context( new SisuBundleContext() );

        bundleContext = context;
        serviceTracker = new ServiceTracker( context, BUNDLE_INJECTOR_CLASS_NAME, this );
        serviceTracker.open();
        bundleTracker = new BundleTracker( context, Bundle.STARTING | Bundle.ACTIVE, this );
        bundleTracker.open();
    }

    public static Injector getInjector( final Bundle bundle )
    {
        try
        {
            return ( (BundleInjector) bundle.getBundleContext().getService( getBundleInjectorService( bundle ) ) ).getInjector();
        }
        catch ( final Throwable e )
        {
            return defaultInjector;
        }
    }

    public void stop( final BundleContext context )
    {
        bundleTracker.close();
        serviceTracker.close();
        locator.clear();

        SisuContainer.context( null );
    }

    // ----------------------------------------------------------------------
    // Bundle tracking
    // ----------------------------------------------------------------------

    public Object addingBundle( final Bundle bundle, final BundleEvent event )
    {
        final Dictionary<?, ?> headers = bundle.getHeaders();
        final String host = (String) headers.get( Constants.FRAGMENT_HOST );
        final String imports = (String) headers.get( Constants.IMPORT_PACKAGE );
        if ( null == host && null != imports && imports.contains( "javax.inject" ) )
        {
            if ( getBundleInjectorService( bundle ) == null )
            {
                new BundleInjector( bundle );
            }
        }
        return null;
    }

    public void modifiedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        // nothing to do
    }

    public void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        // nothing to do
    }

    // ----------------------------------------------------------------------
    // Service tracking
    // ----------------------------------------------------------------------

    @SuppressWarnings( "deprecation" )
    public Object addingService( final ServiceReference reference )
    {
        final Object service = bundleContext.getService( reference );
        locator.add( ( (BundleInjector) service ).getInjector(), 0 );
        return service;
    }

    public void modifiedService( final ServiceReference reference, final Object service )
    {
        // nothing to do
    }

    public void removedService( final ServiceReference reference, final Object service )
    {
        locator.remove( ( (BundleInjector) service ).getInjector() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static ServiceReference getBundleInjectorService( final Bundle bundle )
    {
        final ServiceReference[] serviceReferences = bundle.getRegisteredServices();
        if ( null != serviceReferences )
        {
            for ( final ServiceReference ref : serviceReferences )
            {
                for ( final String name : (String[]) ref.getProperty( Constants.OBJECTCLASS ) )
                {
                    if ( BUNDLE_INJECTOR_CLASS_NAME.equals( name ) )
                    {
                        return ref;
                    }
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class BundleInjector
        implements /* TODO:ManagedService, */Module
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final String[] API = { BUNDLE_INJECTOR_CLASS_NAME /* TODO:, ManagedService.class.getName() */};

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Map<String, String> properties;

        private final Injector injector;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BundleInjector( final Bundle bundle )
        {
            properties = new BundleProperties( bundle.getBundleContext() );

            final ClassSpace space = new BundleClassSpace( bundle );
            final BeanScanning scanning = Main.selectScanning( properties );
            injector = Guice.createInjector( new WireModule( this, new SpaceModule( space, scanning ) ) );
            final Dictionary<Object, Object> metadata = new Hashtable<Object, Object>();
            metadata.put( Constants.SERVICE_PID, "org.sonatype.inject" );

            bundle.getBundleContext().registerService( API, this, metadata );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void configure( final Binder binder )
        {
            binder.bind( ParameterKeys.PROPERTIES ).toInstance( properties );
            binder.bind( MutableBeanLocator.class ).toInstance( locator );
        }

        public Injector getInjector()
        {
            return injector;
        }
    }

    private static final class BundleProperties
        extends HashMap<String, String>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final long serialVersionUID = 1L;

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private transient final BundleContext context;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BundleProperties( final BundleContext context )
        {
            this.context = context;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public String get( final Object key )
        {
            final String value = super.get( key );
            return null != value ? value : context.getProperty( String.valueOf( key ) );
        }
    }
}
