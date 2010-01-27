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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * {@link Iterator} that iterates over entries inside JAR or ZIP resources.
 */
final class ZipEntryIterator
    implements Iterator<String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private Iterator<String> iterator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ZipEntryIterator( final URL url )
    {
        try
        {
            if ( "file".equals( url.getProtocol() ) )
            {
                iterator = iterator( new ZipFile( url.getPath() ) );
            }
            else
            {
                iterator = iterator( new ZipInputStream( url.openStream() ) );
            }
        }
        catch ( final IOException e )
        {
            iterator = ResourceEnumeration.NO_ENTRIES;
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public String next()
    {
        return iterator.next();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns an {@link Iterator} that iterates over the contents of the given zip file.
     * 
     * @param zipFile The zip file
     * @return Iterator that iterates over resources contained inside the given zip file
     */
    private static Iterator<String> iterator( final ZipFile zipFile )
        throws IOException
    {
        try
        {
            final List<String> names = new ArrayList<String>( zipFile.size() );
            for ( final Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); )
            {
                names.add( e.nextElement().getName() );
            }
            return names.iterator();
        }
        finally
        {
            zipFile.close();
        }
    }

    /**
     * Returns an {@link Iterator} that iterates over the contents of the given zip stream.
     * 
     * @param zipStream The zip stream
     * @return Iterator that iterates over resources contained inside the given zip stream
     */
    private static Iterator<String> iterator( final ZipInputStream zipStream )
        throws IOException
    {
        try
        {
            final List<String> names = new ArrayList<String>();
            for ( ZipEntry e = zipStream.getNextEntry(); e != null; e = zipStream.getNextEntry() )
            {
                names.add( e.getName() );
            }
            return names.iterator();
        }
        finally
        {
            zipStream.close();
        }
    }
}