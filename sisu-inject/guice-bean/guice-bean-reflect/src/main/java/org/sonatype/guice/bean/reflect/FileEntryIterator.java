/**
 * Copyright (c) 2009-2011 Sonatype, Inc. All rights reserved.
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

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * {@link Iterator} that iterates over named entries beneath a file-system directory.
 */
final class FileEntryIterator
    implements Iterator<String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String rootPath;

    private final LinkedList<String> entryNames = new LinkedList<String>();

    private final boolean recurse;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates an iterator that iterates over entries beneath the given file URL and sub-path.
     * 
     * @param url The root file URL
     * @param subPath The path below the root URL
     * @param recurse When {@code true} include sub-directories; otherwise don't
     */
    FileEntryIterator( final URL url, final String subPath, final boolean recurse )
    {
        rootPath = normalizePath( toFile( url ).getAbsoluteFile() );
        this.recurse = recurse;
        appendEntries( subPath );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        return entryNames.size() > 0;
    }

    public String next()
    {
        final String name = entryNames.removeFirst();
        if ( recurse && name.endsWith( "/" ) )
        {
            appendEntries( name );
        }
        return name;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Converts a {@link URL} into a {@link File} converting slashes and encoded characters where appropriate.
     * 
     * @param url The file URL
     * @return File instance for the given URL
     */
    static File toFile( final URL url )
    {
        final StringBuilder buf = new StringBuilder();
        final String path = url.getPath();

        for ( int i = 0, length = path.length(); i < length; i++ )
        {
            final char c = path.charAt( i );
            if ( '/' == c )
            {
                buf.append( File.separatorChar );
            }
            else if ( '%' == c && i < length - 2 )
            {
                buf.append( (char) Integer.parseInt( path.substring( ++i, ++i + 1 ), 16 ) );
            }
            else
            {
                buf.append( c );
            }
        }
        return new File( buf.toString() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Appends entries from the given sub-path to the cached list of named entries.
     * 
     * @param subPath The sub path
     */
    private void appendEntries( final String subPath )
    {
        final File[] listing = new File( rootPath + subPath ).listFiles();
        if ( null != listing )
        {
            for ( final File f : listing )
            {
                entryNames.add( normalizePath( f ).substring( rootPath.length() ) );
            }
        }
    }

    /**
     * Returns the normalized URI path of the given file.
     * 
     * @param file The file to normalize
     * @return Normalized URI path
     */
    private static String normalizePath( final File file )
    {
        return file.toURI().getPath();
    }
}
