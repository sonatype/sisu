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
package org.sonatype.guice.bean.extender;

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
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.BundleClassSpace;
import org.sonatype.guice.bean.scanners.QualifiedScannerModule;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.InjectorBuilder;
import com.google.inject.Module;
import com.google.inject.Stage;

public final class Activator
    implements BundleActivator, BundleTrackerCustomizer, ServiceTrackerCustomizer
{
    final MutableBeanLocator beanLocator = new DefaultBeanLocator();

    private final Module locatorModule = new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( BeanLocator.class ).toInstance( beanLocator );
        }
    };

    private BundleContext bundleContext;

    private ServiceTracker serviceTracker;

    private BundleTracker bundleTracker;

    public void start( final BundleContext context )
    {
        bundleContext = context;
        serviceTracker = new ServiceTracker( context, Injector.class.getName(), this );
        serviceTracker.open();
        bundleTracker = new BundleTracker( context, Bundle.ACTIVE, this );
        bundleTracker.open();
    }

    public Object addingBundle( final Bundle bundle, final BundleEvent event )
    {
        final String importPackage = (String) bundle.getHeaders().get( Constants.IMPORT_PACKAGE );
        if ( null != importPackage && importPackage.contains( "javax.inject" ) )
        {
            final InjectorBuilder injectorBuilder = new InjectorBuilder();
            injectorBuilder.stage( Stage.PRODUCTION ); // TODO: hack to get eager singletons
            injectorBuilder.addModules( locatorModule, new QualifiedScannerModule( new BundleClassSpace( bundle ) ) );
            final Injector bundleInjector = injectorBuilder.build();
            if ( bundleInjector.getBindings().size() > 5 ) // TODO: need better check!
            {
                bundle.getBundleContext().registerService( Injector.class.getName(), bundleInjector, null );
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

    public Object addingService( final ServiceReference reference )
    {
        final Object service = bundleContext.getService( reference );
        beanLocator.add( (Injector) service );
        return service;
    }

    public void modifiedService( final ServiceReference reference, final Object service )
    {
        // nothing to do
    }

    public void removedService( final ServiceReference reference, final Object service )
    {
        beanLocator.remove( (Injector) service );
    }

    public void stop( final BundleContext context )
    {
        serviceTracker.close();
        bundleTracker.close();
    }
}
