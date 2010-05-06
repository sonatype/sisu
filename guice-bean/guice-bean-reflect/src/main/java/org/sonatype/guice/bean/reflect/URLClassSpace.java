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
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * {@link ClassSpace} backed by a strongly-referenced {@link ClassLoader} and a search path of {@link URL}s.
 */
public final class URLClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String[] EMPTY_CLASS_PATH = new String[0];

    private static final URL[] EMPTY_URL_CLASS_PATH = new URL[0];

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassLoader loader;

    private final URL[] urls;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link ClassSpace} backed by a {@link URLClassLoader}.
     * 
     * @param loader The URL class loader
     */
    public URLClassSpace( final URLClassLoader loader )
    {
        this( loader, loader.getURLs() );
    }

    /**
     * Creates a {@link ClassSpace} with custom 'get' and 'find' search spaces.
     * 
     * @param loader The class loader to use when getting resources
     * @param urls The path of URLs to search when finding resources
     * @see #getResources(String)
     * @see #findEntries(String, String, boolean)
     */
    public URLClassSpace( final ClassLoader loader, final URL[] urls )
    {
        this.loader = loader;
        this.urls = expandClassPath( urls );
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

    @Override
    public int hashCode()
    {
        return loader.hashCode(); // the loader is the primary key
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof URLClassSpace )
        {
            return loader.equals( ( (URLClassSpace) rhs ).loader );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return loader.toString();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Expands the given URL search path to include Class-Path entries from local manifests.
     * 
     * @param urls The URL search path
     * @return Expanded URL search path
     */
    private static URL[] expandClassPath( final URL[] urls )
    {
        if ( null == urls || urls.length == 0 )
        {
            return EMPTY_URL_CLASS_PATH;
        }

        final LinkedList<URL> searchURLs = new LinkedList<URL>();
        Collections.addAll( searchURLs, urls );

        // search space may grow, so use index not iterator
        final Set<URL> reachableURLs = new LinkedHashSet<URL>();
        while ( !searchURLs.isEmpty() )
        {
            final URL url = searchURLs.removeFirst();
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

        return reachableURLs.toArray( new URL[reachableURLs.size()] );
    }

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
            return EMPTY_CLASS_PATH;
        }
        finally
        {
            in.close();
        }
    }
}