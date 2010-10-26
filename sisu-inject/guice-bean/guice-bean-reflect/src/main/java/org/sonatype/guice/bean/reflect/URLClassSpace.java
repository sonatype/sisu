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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
 * {@link ClassSpace} backed by a strongly-referenced {@link ClassLoader} and a {@link URL} class path.
 */
public final class URLClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String MANIFEST_ENTRY = "META-INF/MANIFEST.MF";

    private static final String[] NO_ENTRIES = {};

    private static final URL[] NO_URLS = {};

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassLoader loader;

    private URL[] classPath;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link ClassSpace} backed by a {@link ClassLoader} and its default class path.<br>
     * For {@link URLClassLoader}s this is their expanded Class-Path; otherwise it is empty.
     * 
     * @param loader The class loader to use when getting/finding resources
     */
    public URLClassSpace( final ClassLoader loader )
    {
        this.loader = loader;
        // compute class path on demand
    }

    /**
     * Creates a {@link ClassSpace} backed by a {@link ClassLoader} with a custom class path.
     * 
     * @param loader The class loader to use when getting resources
     * @param path The class path to use when finding resources
     * @see #getResources(String)
     * @see #findEntries(String, String, boolean)
     */
    public URLClassSpace( final ClassLoader loader, final URL[] path )
    {
        this.loader = loader;
        classPath = expandClassPath( path );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> loadClass( final String name )
    {
        try
        {
            return loader.loadClass( name );
        }
        catch ( final Throwable e )
        {
            throw new TypeNotPresentException( name, e );
        }
    }

    public DeferredClass<?> deferLoadClass( final String name )
    {
        return new NamedClass<Object>( this, name );
    }

    public URL getResource( final String name )
    {
        return loader.getResource( name );
    }

    public Enumeration<URL> getResources( final String name )
    {
        try
        {
            return loader.getResources( name );
        }
        catch ( final IOException e )
        {
            return Collections.enumeration( Collections.<URL> emptyList() );
        }
    }

    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        return new ResourceEnumeration( path, glob, recurse, getClassPath() );
    }

    public URL[] getURLs()
    {
        return getClassPath().clone();
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
     * Returns the associated {@link URL} class path; this can either be explicit or implicit.
     */
    private synchronized URL[] getClassPath()
    {
        if ( null == classPath )
        {
            for ( ClassLoader l = loader; l != null; l = l.getParent() )
            {
                if ( l instanceof URLClassLoader )
                {
                    // pick first class loader with non-empty class path
                    final URL[] path = ( (URLClassLoader) l ).getURLs();
                    if ( null != path && path.length > 0 )
                    {
                        classPath = expandClassPath( path );
                        break;
                    }
                }
            }
            if ( null == classPath )
            {
                classPath = NO_URLS; // no detectable Class-Path
            }
        }
        return classPath;
    }

    /**
     * Expands the given {@link URL} class path to include Class-Path entries from local manifests.
     * 
     * @param classPath The URL class path
     * @return Expanded URL class path
     */
    private static URL[] expandClassPath( final URL[] classPath )
    {
        if ( null == classPath || classPath.length == 0 )
        {
            return NO_URLS;
        }

        final List<URL> searchPath = new ArrayList<URL>();
        Collections.addAll( searchPath, classPath );

        // search path may grow, so use index not iterator
        final Set<URL> expandedPath = new LinkedHashSet<URL>();
        for ( int i = 0; i < searchPath.size(); i++ )
        {
            final URL url = searchPath.get( i );
            if ( null == url || !expandedPath.add( url ) )
            {
                continue; // already processed
            }
            final String[] classPathEntries;
            try
            {
                classPathEntries = getClassPathEntries( url );
            }
            catch ( final IOException e )
            {
                continue; // missing manifest
            }
            for ( final String entry : classPathEntries )
            {
                try
                {
                    searchPath.add( new URL( url, entry ) );
                }
                catch ( final MalformedURLException e ) // NOPMD
                {
                    // invalid Class-Path entry
                }
            }
        }

        return expandedPath.toArray( new URL[expandedPath.size()] );
    }

    /**
     * Looks for Class-Path entries in the given jar or directory; returns empty array if none are found.
     * 
     * @param url The jar or directory to inspect
     * @return Array of Class-Path entries
     */
    private static String[] getClassPathEntries( final URL url )
        throws IOException
    {
        final URL manifestURL;
        if ( url.getPath().endsWith( "/" ) )
        {
            manifestURL = new URL( url, MANIFEST_ENTRY );
        }
        else
        {
            manifestURL = new URL( "jar:" + url + "!/" + MANIFEST_ENTRY );
        }

        final InputStream in = Streams.openStream( manifestURL );
        try
        {
            final String classPath = new Manifest( in ).getMainAttributes().getValue( "Class-Path" );
            return null != classPath ? classPath.split( " " ) : NO_ENTRIES;
        }
        finally
        {
            in.close();
        }
    }
}
