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
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

final class ClassPathEntryEnumeration
    implements Enumeration<URL>
{
    private final URL[] urls;

    private final String path;

    private final Pattern globPattern;

    private final boolean recurse;

    private int index;

    private Iterator<String> entries = Collections.<String> emptyList().iterator();

    private URL parentURL;

    private String cachedEntry;

    ClassPathEntryEnumeration( final URL[] urls, final String path, final String glob, final boolean recurse )
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
                if ( index >= urls.length )
                {
                    return false;
                }
                try
                {
                    entries = iterator( urls[index++] );
                }
                catch ( final IOException e )
                {
                    continue; // try next element
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
                final String entry = cachedEntry;
                cachedEntry = null;
                return new URL( parentURL, entry );
            }
            catch ( final IOException e ) // NOPMD
            {
                // fall-through
            }
        }
        throw new NoSuchElementException();
    }

    private static String normalizePath( final String path )
    {
        if ( null != path )
        {
            return path.replaceFirst( "^/*", "" ).replaceFirst( "//*$", "/" );
        }
        return "/";
    }

    private static Pattern compileGlob( final String glob )
    {
        if ( null == glob || "*".equals( glob ) )
        {
            return null;
        }

        char nextChar;
        char prevChar = '^';

        final StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < glob.length(); i++ )
        {
            nextChar = glob.charAt( i );
            if ( '*' == nextChar )
            {
                if ( '*' == prevChar )
                {
                    continue;
                }
                if ( i > 0 )
                {
                    buf.append( "\\E" );
                }
                buf.append( ".*" );
            }
            else
            {
                if ( 0 == i || '*' == prevChar )
                {
                    buf.append( "\\Q" );
                }
                buf.append( nextChar );
            }
            prevChar = nextChar;
        }
        return Pattern.compile( buf.toString() );
    }

    private Iterator<String> iterator( final URL url )
        throws IOException
    {
        if ( url.getPath().endsWith( ".jar" ) )
        {
            parentURL = new URL( "jar:" + url + "!/" );
            return new ZipEntryIterator( url );
        }
        if ( "file".equals( url.getProtocol() ) )
        {
            parentURL = new URL( url + "/" );
            return new FileEntryIterator( url, path, recurse );
        }
        throw new IOException( "Cannot scan: " + url );
    }

    private boolean matches( final String entry )
    {
        if ( !entry.equals( path ) && !entry.startsWith( path + "/" ) )
        {
            return false;
        }
        if ( !recurse && entry.indexOf( '/', path.length() + 1 ) > 0 )
        {
            return false;
        }
        if ( null == globPattern )
        {
            return true;
        }
        if ( entry.equals( path ) )
        {
            return globPattern.matcher( "/" ).matches();
        }
        final int basenameIndex = 1 + entry.lastIndexOf( '/', entry.length() - 2 );
        return globPattern.matcher( entry.substring( basenameIndex ) ).matches();
    }
}