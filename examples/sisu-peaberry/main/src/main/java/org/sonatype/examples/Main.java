/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public final class Main
{
    public static void main( final String[] args )
        throws Exception
    {
        if ( true || args.length > 0 && "osgi".equalsIgnoreCase( args[0] ) )
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
            throws Exception
        {
            final Class<?> mainClazz = Class.forName( "org.eclipse.sisu.launch.Main" );
            mainClazz.getMethod( "main", String[].class ).invoke( null, (Object) new String[0] );
        }
    }

    static class OSGiLauncher
    {
        static void launch()
            throws BundleException
        {
            final Map<String, String> configuration = new HashMap<String, String>();

            configuration.put( Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT );
            configuration.put( Constants.FRAMEWORK_STORAGE, "target/bundlecache" );

            configuration.putAll( (Map)System.getProperties() );

            final FrameworkFactory frameworkFactory = ServiceRegistry.lookupProviders( FrameworkFactory.class ).next();
            final Framework framework = frameworkFactory.newFramework( configuration );

            framework.start();

            final BundleContext ctx = framework.getBundleContext();

            final List<Bundle> bundles = new ArrayList<Bundle>();
            for ( final File f : new File( getTargetDir(), "lib" ).listFiles() )
            {
                if ( !f.getName().contains( "framework" ) )
                {
                    bundles.add( ctx.installBundle( "reference:" + f.toURI() ) );
                }
            }
            for ( final Bundle bundle : bundles )
            {
                if ( bundle.getHeaders().get( Constants.FRAGMENT_HOST ) == null )
                {
                    bundle.start();
                }
            }
        }
    }

    static File getTargetDir()
    {
        final String clazz = Main.class.getName().replace( '.', '/' ) + ".class";
        final String base = Main.class.getClassLoader().getResource( clazz ).getPath();
        return new File( base.substring( 5, base.lastIndexOf( "/main.jar" ) ) );
    }
}
