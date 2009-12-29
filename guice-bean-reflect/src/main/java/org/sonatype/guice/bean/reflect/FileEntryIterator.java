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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link Iterator} that iterates over entries beneath file/directory resources.
 */
final class FileEntryIterator
    implements Iterator<String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String rootPath;

    private final List<String> entries = new ArrayList<String>();

    private final boolean recurse;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    FileEntryIterator( final URL url, final String subPath, final boolean recurse )
    {
        // allow the entry search to start from a specific sub-path
        final File rootFile = new File( url.getPath() ).getAbsoluteFile();
        final File fromFile = null != subPath ? new File( rootFile, subPath ) : rootFile;

        // use canonical paths to try and detect cycles caused by symlinks, etc
        rootPath = normalizePath( rootFile.isFile() ? rootFile.getParentFile() : rootFile );
        final String fromPath = normalizePath( fromFile );

        if ( fromPath.endsWith( "/" ) )
        {
            addChildren( fromPath ); // directory listing
        }
        else if ( fromFile.exists() )
        {
            addEntry( fromPath ); // single file listing
        }

        this.recurse = recurse;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        return !entries.isEmpty();
    }

    public String next()
    {
        if ( hasNext() )
        {
            final String entry = entries.remove( 0 );
            if ( recurse && entry.endsWith( "/" ) )
            {
                // unroll listing as we iterate
                addChildren( rootPath + entry );
            }
            return entry;
        }
        throw new NoSuchElementException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Adds children of the given entry whose canonical paths are located below the entry.
     * 
     * @param path The entry path
     */
    private void addChildren( final String path )
    {
        final File[] listing = new File( path ).listFiles();
        if ( null == listing )
        {
            return; // no children to add
        }
        for ( final File f : listing )
        {
            // check canonical path len to avoid cycles
            final String childPath = normalizePath( f );
            if ( childPath.length() > path.length() )
            {
                addEntry( childPath );
            }
        }
    }

    /**
     * Adds the given entry if it is located below the root; otherwise ignores the entry.
     * 
     * @param path The entry path
     */
    private void addEntry( final String path )
    {
        // make sure entry is in our scope
        if ( path.startsWith( rootPath ) )
        {
            entries.add( path.substring( rootPath.length() ) );
        }
    }

    /**
     * Returns the unique normalized URI form of the given path.
     * 
     * @param file The path to normalize
     * @return Normalized URI-form path
     */
    private static String normalizePath( final File file )
    {
        try
        {
            return file.getCanonicalFile().toURI().getPath();
        }
        catch ( final IOException e )
        {
            return file.toURI().getPath();
        }
    }
}