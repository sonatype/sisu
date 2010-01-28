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
package org.sonatype.guice.bean.reflect;

import static org.sonatype.guice.bean.reflect.ResourceEnumeration.entryURL;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * {@link ClassSpace} backed by a strongly-referenced {@link URLClassLoader}.
 */
public final class URLClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String[] NO_CLASS_PATH = new String[0];

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final URLClassLoader loader;

    private final URL[] urls;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public URLClassSpace( final URLClassLoader loader )
    {
        this.loader = loader;

        // discover all URLs reachable from the given list
        final List<URL> searchURLs = new ArrayList<URL>();
        Collections.addAll( searchURLs, loader.getURLs() );

        final Set<URL> reachableURLs = new LinkedHashSet<URL>();

        // search may grow, so use index not iterator
        for ( int i = 0; i < searchURLs.size(); i++ )
        {
            final URL url = searchURLs.get( i );
            if ( !reachableURLs.add( url ) )
            {
                continue; // already processed
            }
            final String[] classPath;
            try
            {
                // search Class-Path entries in manifest in case they refer to other JARs/folders
                classPath = getClassPath( entryURL( url, "META-INF/MANIFEST.MF" ).openStream() );
            }
            catch ( final IOException e ) // NOPMD
            {
                continue; // corrupt manifest
            }
            for ( final String entry : classPath )
            {
                try
                {
                    searchURLs.add( new URL( url, entry ) );
                }
                catch ( final IOException e ) // NOPMD
                {
                    // invalid Class-Path entry
                }
            }
        }

        urls = reachableURLs.toArray( new URL[reachableURLs.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> loadClass( final String name )
        throws ClassNotFoundException
    {
        return loader.loadClass( name );
    }

    public DeferredClass<?> deferLoadClass( final String name )
    {
        return new StrongDeferredClass<Object>( this, name );
    }

    public URL getResource( final String name )
    {
        return loader.getResource( name );
    }

    public Enumeration<URL> getResources( final String name )
        throws IOException
    {
        return loader.getResources( name );
    }

    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        return new ResourceEnumeration( path, glob, recurse, urls );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Looks for Class-Path entries in the given manifest stream; returns empty array if none are found.
     * 
     * @param in The manifest input stream
     * @return Array of class-path entries
     */
    private static String[] getClassPath( final InputStream in )
        throws IOException
    {
        try
        {
            final String classPath = new Manifest( in ).getMainAttributes().getValue( "Class-Path" );
            if ( null != classPath )
            {
                return classPath.split( " " );
            }
            return NO_CLASS_PATH;
        }
        finally
        {
            in.close();
        }
    }
}