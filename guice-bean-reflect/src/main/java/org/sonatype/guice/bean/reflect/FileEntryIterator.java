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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

final class FileEntryIterator
    implements Iterator<String>
{
    private String rootPath;

    private final List<String> entries = new ArrayList<String>();

    private final boolean recurse;

    public FileEntryIterator( final URL url, final String path, final boolean recurse )
    {
        try
        {
            rootPath = canonicalPath( new File( url.getPath() ) );
            final String fromPath = canonicalPath( new File( rootPath, path ) );
            if ( fromPath.startsWith( rootPath ) )
            {
                entries.add( fromPath.substring( rootPath.length() ) );
                if ( !recurse )
                {
                    unrollListing( fromPath );
                }
            }
        }
        catch ( final Exception e ) // NOPMD
        {
            // empty-iterator
        }
        this.recurse = recurse;
    }

    public boolean hasNext()
    {
        return !entries.isEmpty();
    }

    public String next()
    {
        if ( hasNext() )
        {
            final String entry = entries.remove( 0 );
            if ( recurse )
            {
                unrollListing( rootPath + entry );
            }
            return entry;
        }
        throw new NoSuchElementException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private void unrollListing( final String parentPath )
    {
        final File[] listing = new File( parentPath ).listFiles();
        if ( null == listing )
        {
            return;
        }
        for ( final File f : listing )
        {
            try
            {
                final String entryPath = canonicalPath( f );
                if ( entryPath.startsWith( parentPath ) && entryPath.length() > parentPath.length() )
                {
                    entries.add( entryPath.substring( rootPath.length() ) );
                }
            }
            catch ( final Exception e )
            {
                continue;
            }
        }
    }

    private String canonicalPath( final File file )
        throws IOException
    {
        if ( !file.exists() )
        {
            throw new FileNotFoundException();
        }
        String path = file.getCanonicalPath();
        if ( File.separatorChar != '/' )
        {
            path = path.replace( File.separatorChar, '/' );
        }
        if ( file.isDirectory() )
        {
            path = path + '/';
        }
        return path;
    }
}