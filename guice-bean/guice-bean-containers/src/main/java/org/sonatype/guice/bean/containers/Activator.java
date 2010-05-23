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
import org.sonatype.guice.bean.binders.BeanImportModule;
import org.sonatype.guice.bean.binders.BeanSpaceModule;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.BundleClassSpace;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.InjectorBuilder;
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

    private static final int BASE_BINDINGS_SIZE = Guice.createInjector().getBindings().size() + 1;

    private static final String INJECTOR_CLASS = Injector.class.getName();

    private static final LocatorModule LOCATOR_MODULE = new LocatorModule();

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
        serviceTracker = new ServiceTracker( context, INJECTOR_CLASS, this );
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
        final String importPackage = (String) bundle.getHeaders().get( Constants.IMPORT_PACKAGE );
        if ( null == importPackage || !importPackage.contains( "javax.inject" ) )
        {
            return null; // bundle doesn't import @Inject, so won't have any beans
        }
        final ServiceReference[] serviceReferences = bundle.getRegisteredServices();
        if ( null != serviceReferences )
        {
            for ( final ServiceReference ref : serviceReferences )
            {
                if ( INJECTOR_CLASS.equals( ( (String[]) ref.getProperty( Constants.OBJECTCLASS ) )[0] ) )
                {
                    return null; // this bundle already has an injector registered
                }
            }
        }
        final Module beanModule = new BeanImportModule( new BeanSpaceModule( new BundleClassSpace( bundle ) ) );
        final Injector bundleInjector = new InjectorBuilder().addModules( LOCATOR_MODULE, beanModule ).build();
        if ( bundleInjector.getBindings().size() > BASE_BINDINGS_SIZE )
        {
            bundle.getBundleContext().registerService( INJECTOR_CLASS, bundleInjector, null );
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

    public Object addingService( final ServiceReference reference )
    {
        final Object service = bundleContext.getService( reference );
        LOCATOR_MODULE.add( (Injector) service );
        return service;
    }

    public void modifiedService( final ServiceReference reference, final Object service )
    {
        // nothing to do
    }

    public void removedService( final ServiceReference reference, final Object service )
    {
        LOCATOR_MODULE.remove( (Injector) service );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    static final class LocatorModule
        implements Module
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final MutableBeanLocator locator = new DefaultBeanLocator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void configure( final Binder binder )
        {
            binder.bind( BeanLocator.class ).toInstance( locator );
        }

        public void add( final Injector injector )
        {
            locator.add( injector );
        }

        public void remove( final Injector injector )
        {
            locator.remove( injector );
        }
    }
}
