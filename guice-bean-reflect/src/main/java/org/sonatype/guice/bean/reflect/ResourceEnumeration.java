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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * {@link Enumeration} of resource {@link URL}s that match globbed search patterns.
 */
final class ResourceEnumeration
    implements Enumeration<URL>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Iterator<String> NO_STRINGS = Collections.<String> emptyList().iterator();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final URL[] urls;

    private final String subPath;

    private final Pattern globPattern;

    private final boolean recurse;

    private int index = -1;

    private Iterator<String> entries = NO_STRINGS;

    private String cachedEntry;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates an {@link Enumeration} that scans the given URLs for resources matching the globbed pattern.
     * 
     * @param urls The URLs containing resources
     * @param subPath An optional path to begin the search from
     * @param glob The globbed basename pattern
     * @param recurse When {@code true} search paths below the initial search point; otherwise don't
     */
    ResourceEnumeration( final URL[] urls, final String subPath, final String glob, final boolean recurse )
    {
        this.urls = urls;
        this.subPath = normalizeSearchPath( subPath );
        globPattern = compileGlobPattern( glob );
        this.recurse = recurse;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasMoreElements()
    {
        while ( null == cachedEntry )
        {
            // URL still has resources
            if ( entries.hasNext() )
            {
                cachedEntry = entries.next();
                if ( !matches( cachedEntry ) )
                {
                    // reset, try again
                    cachedEntry = null;
                }
            }
            else
            {
                if ( index < urls.length - 1 )
                {
                    // try the next URL in the list
                    entries = iterator( urls[++index] );
                }
                else
                {
                    // no more URLs
                    return false;
                }
            }
        }
        return true;
    }

    public URL nextElement()
    {
        if ( hasMoreElements() )
        {
            try
            {
                return entryURL( urls[index], cachedEntry );
            }
            catch ( final MalformedURLException e )
            {
                // this shouldn't happen, hence illegal state
                throw new IllegalStateException( e.toString() );
            }
            finally
            {
                // remember to reset
                cachedEntry = null;
            }
        }
        throw new NoSuchElementException();
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    /**
     * Returns a {@link URL} that points to the given entry inside the containing URL.
     * 
     * @param url The containing URL
     * @param path The entry path
     * @return URL pointing to the entry
     */
    static URL entryURL( final URL url, final String path )
        throws MalformedURLException
    {
        return url.getPath().endsWith( "/" ) ? new URL( url, path ) : new URL( "jar:" + url + "!/" + path );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Normalizes the initial search path by removing any duplicate or initial slashes.
     * 
     * @param path The path to normalize
     * @return Normalized search path
     */
    private static String normalizeSearchPath( final String path )
    {
        if ( null == path || "/".equals( path ) )
        {
            return "";
        }
        return ( '/' + path + '/' ).replaceAll( "/+", "/" ).substring( 1 );
    }

    /**
     * Compiles the given globbed pattern into a regular expression pattern.
     * 
     * @param glob The globbed pattern
     * @return The matching regular expression
     */
    private static Pattern compileGlobPattern( final String glob )
    {
        if ( null == glob || "*".equals( glob ) )
        {
            return null;
        }
        return Pattern.compile( glob.replaceAll( "[^*]+", "\\\\Q$0\\\\E" ).replaceAll( "\\*+", ".*" ) );
    }

    /**
     * Returns the appropriate {@link Iterator} to iterator over the contents of the given URL.
     * 
     * @param url The containing URL
     * @return Iterator that iterates over resources contained inside the given URL
     */
    private Iterator<String> iterator( final URL url )
    {
        if ( url.getPath().endsWith( "/" ) )
        {
            return new FileEntryIterator( url, subPath, recurse );
        }
        return new ZipEntryIterator( url );
    }

    /**
     * @param entryPath The entry path
     * @return {@code true} if the given path matches the search pattern; otherwise {@code false}
     */
    private boolean matches( final String entryPath )
    {
        if ( !entryPath.startsWith( subPath ) || entryPath.length() == subPath.length() )
        {
            return false; // not inside the search scope
        }
        if ( !recurse )
        {
            final int nextSlashIndex = entryPath.indexOf( '/', subPath.length() );
            if ( 0 < nextSlashIndex && nextSlashIndex < entryPath.length() - 1 )
            {
                return false; // inside a sub-directory
            }
        }
        if ( null == globPattern )
        {
            return true; // null pattern always matches
        }

        // globbed pattern only applies to the last segment of the entry path
        final int basenameIndex = 1 + entryPath.lastIndexOf( '/', entryPath.length() - 2 );
        return globPattern.matcher( entryPath.substring( basenameIndex ) ).matches();
    }
}
