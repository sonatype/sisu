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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

/**
 * {@link ClassSpace} backed by a strongly-referenced {@link URLClassLoader}.
 */
public final class URLClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final URLClassLoader space;

    private final URL[] urls;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public URLClassSpace( final URLClassLoader space )
    {
        this.space = space;

        final List<URL> searchURLs = new ArrayList<URL>();
        Collections.addAll( searchURLs, space.getURLs() );

        // expand search URLs to include Class-Path entries
        final List<URL> expandedURLs = new ArrayList<URL>();
        for ( int i = 0; i < searchURLs.size(); i++ )
        {
            final URL url = searchURLs.get( i );
            expandedURLs.add( url );

            try
            {
                // add URLs referenced in Class-Path header from the associated manifest file
                final Manifest manifest = new Manifest( entryURL( url, "META-INF/MANIFEST.MF" ).openStream() );
                final String classPath = manifest.getMainAttributes().getValue( "Class-Path" );
                if ( null == classPath )
                {
                    continue; // nothing extra to add
                }
                for ( final String entry : classPath.split( " " ) )
                {
                    final URL entryURL = new URL( url, entry );
                    if ( searchURLs.contains( entryURL ) )
                    {
                        continue; // already processed
                    }
                    searchURLs.add( entryURL );
                }
            }
            catch ( final IOException e ) // NOPMD
            {
                // move onto next URL
            }
        }

        urls = expandedURLs.toArray( new URL[expandedURLs.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> loadClass( final String name )
        throws ClassNotFoundException
    {
        return space.loadClass( name );
    }

    public DeferredClass<?> deferLoadClass( final String name )
    {
        return new StrongDeferredClass<Object>( this, name );
    }

    public Enumeration<URL> getResources( final String name )
        throws IOException
    {
        return space.getResources( name );
    }

    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        return new ResourceEnumeration( urls, path, glob, recurse );
    }
}