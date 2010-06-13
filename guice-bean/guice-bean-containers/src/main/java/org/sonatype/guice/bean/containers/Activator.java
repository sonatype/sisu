/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.containers;

import java.util.Arrays;
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
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.BundleClassSpace;
import org.sonatype.guice.bean.reflect.ClassSpace;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * {@link BundleActivator} that maintains a dynamic {@link Injector} graph by scanning bundles as they come and go.
 */
public final class Activator
    implements BundleActivator, BundleTrackerCustomizer, ServiceTrackerCustomizer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String BUNDLE_INJECTOR_CLASS = BundleInjector.class.getName();

    static final MutableBeanLocator LOCATOR = new DefaultBeanLocator();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private BundleContext bundleContext;

    private ServiceTracker serviceTracker;

    private BundleTracker bundleTracker;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final BundleContext context )
    {
        bundleContext = context;
        serviceTracker = new ServiceTracker( context, BUNDLE_INJECTOR_CLASS, this );
        serviceTracker.open();
        bundleTracker = new BundleTracker( context, Bundle.ACTIVE, this );
        bundleTracker.open();
    }

    public void stop( final BundleContext context )
    {
        bundleTracker.close();
        serviceTracker.close();
    }

    // ----------------------------------------------------------------------
    // Bundle tracking
    // ----------------------------------------------------------------------

    public Object addingBundle( final Bundle bundle, final BundleEvent event )
    {
        final String symbolicName = bundle.getSymbolicName();
        if ( "com.google.inject".equals( symbolicName ) || "org.sonatype.inject".equals( symbolicName ) )
        {
            return null; // don't bother scanning either of these infrastructure bundles
        }

        final String imports = (String) bundle.getHeaders().get( Constants.IMPORT_PACKAGE );
        if ( null == imports || !imports.contains( "javax.inject" ) )
        {
            return null; // bundle doesn't import @Inject, so won't have any beans
        }

        final ServiceReference[] serviceReferences = bundle.getRegisteredServices();
        if ( null != serviceReferences )
        {
            for ( final ServiceReference ref : serviceReferences )
            {
                if ( Arrays.asList( ref.getProperty( Constants.OBJECTCLASS ) ).contains( BUNDLE_INJECTOR_CLASS ) )
                {
                    return null; // this bundle already has an injector registered
                }
            }
        }

        // bootstrap new injector
        new BundleInjector( bundle );

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

    public Object addingService( final ServiceReference reference )
    {
        final Object service = bundleContext.getService( reference );
        LOCATOR.add( ( (BundleInjector) service ).getInjector() );
        return service;
    }

    public void modifiedService( final ServiceReference reference, final Object service )
    {
        // nothing to do
    }

    public void removedService( final ServiceReference reference, final Object service )
    {
        LOCATOR.remove( ( (BundleInjector) service ).getInjector() );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private static final class BundleInjector
        implements /* TODO:ManagedService, */Module
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final String[] API = { BUNDLE_INJECTOR_CLASS /* TODO:, ManagedService.class.getName() */};

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Map<String, ?> config;

        private final Injector injector;

        private final ServiceRegistration registration;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BundleInjector( final Bundle bundle )
        {
            config = new BundleConfig( bundle );

            final ClassSpace space = new BundleClassSpace( bundle );
            injector = Guice.createInjector( new WireModule( this, new SpaceModule( space ) ) );
            final Dictionary<Object, Object> metadata = new Hashtable<Object, Object>();
            metadata.put( Constants.SERVICE_PID, WireModule.CONFIG_KEY );

            registration = bundle.getBundleContext().registerService( API, this, metadata );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void configure( final Binder binder )
        {
            binder.bind( WireModule.CONFIG_KEY ).toInstance( config );
            binder.bind( MutableBeanLocator.class ).toInstance( LOCATOR );
        }

        public Injector getInjector()
        {
            return injector;
        }

        @SuppressWarnings( "unused" )
        public void updated( final Dictionary newConfig )
        {
            config.clear();

            if ( null != newConfig )
            {
                config.putAll( (Map) newConfig );
                // TODO: reconstruct injector?
            }

            registration.setProperties( new Hashtable( config ) );
        }
    }

    private static final class BundleConfig
        extends HashMap<String, Object>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final long serialVersionUID = 1L;

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final BundleContext context;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BundleConfig( final Bundle bundle )
        {
            context = bundle.getBundleContext();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Object get( final Object key )
        {
            final Object value = super.get( key );
            return null != value ? value : context.getProperty( String.valueOf( key ) );
        }
    }
}
