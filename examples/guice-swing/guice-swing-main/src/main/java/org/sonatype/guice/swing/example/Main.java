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
package org.sonatype.guice.swing.example;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;
import javax.swing.SwingUtilities;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.sonatype.guice.bean.locators.GuiceBeanLocator;
import org.sonatype.guice.bean.reflect.BundleClassSpace;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.QualifiedScannerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

public final class Main
{
    public static void main( final String[] args )
        throws Exception
    {
        if ( args.length > 0 && "osgi".equalsIgnoreCase( args[0] ) )
        {
            OSGiLauncher.launch();
        }
        else
        {
            ClassicLauncher.launch();
        }
    }

    static class ClassicLauncher
    {
        static void launch()
        {
            final ClassSpace space = new URLClassSpace( (URLClassLoader) Main.class.getClassLoader() );
            final Injector injector = Guice.createInjector( new QualifiedScannerModule( space ) );
            SwingUtilities.invokeLater( injector.getInstance( Runnable.class ) );
        }
    }

    static class OSGiLauncher
    {
        static void launch()
            throws BundleException
        {
            final Map<String, String> configuration = new HashMap<String, String>();
            configuration.put( Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "org.aopalliance.intercept,javax.inject" );
            configuration.put( Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT );
            configuration.put( Constants.FRAMEWORK_STORAGE, "target/bundlecache" );

            final FrameworkFactory frameworkFactory = ServiceRegistry.lookupProviders( FrameworkFactory.class ).next();
            final Framework framework = frameworkFactory.newFramework( configuration );

            framework.start();

            final Injector parent = Guice.createInjector();
            final GuiceBeanLocator locator = parent.getInstance( GuiceBeanLocator.class );
            final BundleContext ctx = framework.getBundleContext();

            final List<Bundle> bundles = new ArrayList<Bundle>();
            for ( final File f : new File( getTargetDir(), "lib" ).listFiles() )
            {
                final String name = f.getName();
                if ( name.startsWith( "guice-swing" ) || name.contains( "shell" ) )
                {
                    bundles.add( ctx.installBundle( "reference:" + f.toURI() ) );
                }
            }
            for ( final Bundle bundle : bundles )
            {
                final ClassSpace space = new BundleClassSpace( bundle );
                locator.add( parent.createChildInjector( new QualifiedScannerModule( space ) ) );
                bundle.start();
            }

            SwingUtilities.invokeLater( locator.locate( Key.get( Runnable.class ) ).iterator().next().getValue() );
        }
    }

    static File getTargetDir()
    {
        final String clazz = Main.class.getName().replace( '.', '/' ) + ".class";
        final String base = Main.class.getClassLoader().getResource( clazz ).getPath();
        return new File( base.substring( 5, base.lastIndexOf( "/main.jar" ) ) );
    }
}
