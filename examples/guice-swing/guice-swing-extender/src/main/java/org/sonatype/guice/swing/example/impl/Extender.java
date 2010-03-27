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
package org.sonatype.guice.swing.example.impl;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.GuiceBeanLocator;
import org.sonatype.guice.bean.reflect.BundleClassSpace;
import org.sonatype.guice.bean.scanners.QualifiedScannerModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public final class Extender
    implements BundleActivator, BundleTrackerCustomizer
{
    final GuiceBeanLocator beanLocator = new GuiceBeanLocator();

    final Map<Bundle, Injector> injectorMap = new HashMap<Bundle, Injector>();

    final Module locatorModule = new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( BeanLocator.class ).toInstance( beanLocator );
        }
    };

    BundleTracker bundleTracker;

    public void start( final BundleContext context )
    {
        bundleTracker = new BundleTracker( context, Bundle.ACTIVE, this );
        bundleTracker.open();
    }

    public Object addingBundle( final Bundle bundle, final BundleEvent event )
    {
        final Injector bundleInjector =
            Guice.createInjector( locatorModule, new QualifiedScannerModule( new BundleClassSpace( bundle ) ) );

        beanLocator.add( bundleInjector );
        injectorMap.put( bundle, bundleInjector );
        if ( "org.sonatype.spice.inject.examples.guice-swing-window".equals( bundle.getSymbolicName() ) )
        {
            SwingUtilities.invokeLater( bundleInjector.getInstance( Runnable.class ) );
        }

        return bundle;
    }

    public void modifiedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
    }

    public void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        beanLocator.remove( injectorMap.remove( bundle ) );
    }

    public void stop( final BundleContext context )
    {
        bundleTracker.close();
    }
}
