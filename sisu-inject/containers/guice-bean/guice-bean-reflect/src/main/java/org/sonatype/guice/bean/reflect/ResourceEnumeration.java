/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Enumeration} of resources found by scanning JARs and directories.
 */
final class ResourceEnumeration
    implements Enumeration<URL>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Iterator<String> NO_ENTRIES = Collections.<String> emptyList().iterator();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final URL[] urls;

    private final String subPath;

    private final GlobberStrategy globber;

    private final Object globPattern;

    private final boolean recurse;

    private int index;

    private URL currentURL;

    private boolean isFolder;

    private Iterator<String> entryNames = NO_ENTRIES;

    private String nextEntryName;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates an {@link Enumeration} that scans the given URLs for resources matching the globbed pattern.
     * 
     * @param subPath An optional path to begin the search from
     * @param glob The globbed basename pattern
     * @param recurse When {@code true} search paths below the initial search point; otherwise don't
     * @param urls The URLs containing resources
     */
    ResourceEnumeration( final String subPath, final String glob, final boolean recurse, final URL[] urls )
    {
        this.subPath = normalizeSearchPath( subPath );
        globber = GlobberStrategy.selectFor( glob );
        globPattern = globber.compile( glob );
        this.recurse = recurse;
        this.urls = urls;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasMoreElements()
    {
        while ( null == nextEntryName )
        {
            if ( entryNames.hasNext() )
            {
                final String name = entryNames.next();
                if ( matchesRequest( name ) )
                {
                    nextEntryName = name;
                }
            }
            else if ( index < urls.length )
            {
                currentURL = urls[index++];
                entryNames = scan( currentURL );
            }
            else
            {
                return false; // no more URLs
            }
        }
        return true;
    }

    public URL nextElement()
    {
        if ( hasMoreElements() )
        {
            // initialized by hasMoreElements()
            final String name = nextEntryName;
            nextEntryName = null;

            try
            {
                return getResource( name );
            }
            catch ( final MalformedURLException e )
            {
                // this shouldn't happen, hence illegal state
                throw new IllegalStateException( e.toString() );
            }
        }
        throw new NoSuchElementException();
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
        boolean echoSlash = false;
        final StringBuilder buf = new StringBuilder();
        for ( int i = 0, length = path.length(); i < length; i++ )
        {
            // ignore any duplicate slashes
            final char c = path.charAt( i );
            final boolean isNotSlash = '/' != c;
            if ( echoSlash || isNotSlash )
            {
                echoSlash = isNotSlash;
                buf.append( c );
            }
        }
        if ( echoSlash )
        {
            // add final slash
            buf.append( '/' );
        }
        return buf.toString();
    }

    /**
     * Returns the appropriate {@link Iterator} to iterate over the contents of the given URL.
     * 
     * @param url The containing URL
     * @return Iterator that iterates over resources contained inside the given URL
     */
    private Iterator<String> scan( final URL url )
    {
        isFolder = url.getPath().endsWith( "/" );

        if ( globber == GlobberStrategy.EXACT && !recurse )
        {
            try
            {
                // short-cut the nextElement() process
                nextEntryName = subPath + globPattern;

                // but still need to check resource actually exists!
                Streams.open( getResource( nextEntryName ) ).close();
            }
            catch ( final Throwable e )
            {
                nextEntryName = null;
            }
            return NO_ENTRIES;
        }

        return isFolder ? new FileEntryIterator( url, subPath, recurse ) : new ZipEntryIterator( url );
    }

    /**
     * Returns a {@link URL} pointing to the named resource underneath the current search URL.
     * 
     * @param name The resource name
     * @return URL for the resource
     */
    private URL getResource( final String name )
        throws MalformedURLException
    {
        return isFolder ? new URL( currentURL, name ) : new URL( "jar:" + currentURL + "!/" + name );
    }

    /**
     * Compares the given entry name against the normalized search path and compiled glob pattern.
     * 
     * @param entryName The entry name
     * @return {@code true} if the given name matches the search criteria; otherwise {@code false}
     */
    private boolean matchesRequest( final String entryName )
    {
        if ( entryName.endsWith( "/" ) || !entryName.startsWith( subPath ) )
        {
            return false; // not inside the search scope
        }
        if ( !recurse && entryName.indexOf( '/', subPath.length() ) > 0 )
        {
            return false; // inside a sub-directory
        }
        return globber.matches( globPattern, entryName );
    }
}
