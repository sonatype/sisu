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

final class ResourceEnumeration
    implements Enumeration<URL>
{
    private static final StringPartitioner PATH_PARTITIONER = new StringPartitioner( '/', "", "/", "" );

    private static final StringPartitioner GLOB_PARTITIONER = new StringPartitioner( '*', "\\Q", "\\E", ".*" );

    private final URL[] urls;

    private final String path;

    private final Pattern globPattern;

    private final boolean recurse;

    private int index = -1;

    private Iterator<String> entries = Collections.<String> emptyList().iterator();

    private String cachedEntry;

    ResourceEnumeration( final URL[] urls, final String path, final String glob, final boolean recurse )
    {
        this.urls = urls;
        this.path = normalizePath( path );
        globPattern = compileGlob( glob );
        this.recurse = recurse;
    }

    public boolean hasMoreElements()
    {
        while ( null == cachedEntry )
        {
            if ( entries.hasNext() )
            {
                cachedEntry = entries.next();
                if ( !matches( cachedEntry ) )
                {
                    cachedEntry = null;
                }
            }
            else
            {
                if ( ++index >= urls.length )
                {
                    return false;
                }
                entries = iterator( urls[index] );
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
                final URL url = urls[index];
                if ( entries instanceof ZipEntryIterator )
                {
                    return new URL( "jar:" + url + "!/" + cachedEntry );
                }
                return new URL( url, cachedEntry );
            }
            catch ( final MalformedURLException e )
            {
                // this shouldn't happen, hence illegal state
                throw new IllegalStateException( e.toString() );
            }
            finally
            {
                cachedEntry = null;
            }
        }
        throw new NoSuchElementException();
    }

    private static String normalizePath( final String path )
    {
        if ( null == path || "/".equals( path ) )
        {
            return "";
        }
        return PATH_PARTITIONER.partition( path );
    }

    private static Pattern compileGlob( final String glob )
    {
        if ( null == glob || "*".equals( glob ) )
        {
            return null;
        }
        return Pattern.compile( GLOB_PARTITIONER.partition( glob ) );
    }

    private Iterator<String> iterator( final URL url )
    {
        if ( url.getPath().endsWith( ".jar" ) )
        {
            return new ZipEntryIterator( url );
        }
        if ( "file".equals( url.getProtocol() ) )
        {
            return new FileEntryIterator( url, path, recurse );
        }
        return Collections.<String> emptyList().iterator();
    }

    private boolean matches( final String entry )
    {
        if ( !entry.startsWith( path ) || entry.equals( path ) )
        {
            return false;
        }
        if ( !recurse )
        {
            final int nextSlashIndex = entry.indexOf( '/', path.length() );
            if ( 0 < nextSlashIndex && nextSlashIndex < entry.length() - 1 )
            {
                return false;
            }
        }
        if ( null == globPattern )
        {
            return true;
        }
        final int basenameIndex = 1 + entry.lastIndexOf( '/', entry.length() - 2 );
        return globPattern.matcher( entry.substring( basenameIndex ) ).matches();
    }
}