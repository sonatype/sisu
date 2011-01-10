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
 * {@link Iterator} that iterates over named entries inside JAR or ZIP resources.
 */
final class ZipEntryIterator
    implements Iterator<String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String[] entryNames;

    private int index;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ZipEntryIterator( final URL url )
    {
        try
        {
            if ( "file".equals( url.getProtocol() ) )
            {
                entryNames = getEntryNames( new ZipFile( FileEntryIterator.toFile( url ) ) );
            }
            else
            {
                entryNames = getEntryNames( new ZipInputStream( Streams.open( url ) ) );
            }
        }
        catch ( final IOException e )
        {
            entryNames = new String[0];
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        return index < entryNames.length;
    }

    public String next()
    {
        return entryNames[index++];
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns a string array listing the entries in the given zip file.
     * 
     * @param zipFile The zip file
     * @return Array of entry names
     */
    private static String[] getEntryNames( final ZipFile zipFile )
        throws IOException
    {
        try
        {
            final String names[] = new String[zipFile.size()];
            final Enumeration<? extends ZipEntry> e = zipFile.entries();
            for ( int i = 0; i < names.length; i++ )
            {
                names[i] = e.nextElement().getName();
            }
            return names;
        }
        finally
        {
            zipFile.close();
        }
    }

    /**
     * Returns a string array listing the entries in the given zip stream.
     * 
     * @param zipStream The zip stream
     * @return Array of entry names
     */
    private static String[] getEntryNames( final ZipInputStream zipStream )
        throws IOException
    {
        try
        {
            final List<String> names = new ArrayList<String>( 64 );
            for ( ZipEntry e = zipStream.getNextEntry(); e != null; e = zipStream.getNextEntry() )
            {
                names.add( e.getName() );
            }
            return names.toArray( new String[names.size()] );
        }
        finally
        {
            zipStream.close();
        }
    }
}
