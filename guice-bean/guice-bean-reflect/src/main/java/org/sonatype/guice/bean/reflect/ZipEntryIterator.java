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
import java.util.Iterator;
import java.util.NoSuchElementException;
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

    private ZipInputStream in;

    private String nextPath;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ZipEntryIterator( final URL url )
    {
        try
        {
            in = new ZipInputStream( url.openStream() );
        }
        catch ( final IOException e ) // NOPMD
        {
            // empty-iterator
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        if ( null != nextPath )
        {
            return true; // already cached
        }
        try
        {
            // populate cache with the next element
            nextPath = in.getNextEntry().getName();
            return true;
        }
        catch ( final Exception e ) // NOPMD
        {
            // end-of-stream
        }
        try
        {
            // cleanup
            in.close();
            in = null;
        }
        catch ( final Exception e ) // NOPMD
        {
            // ignore
        }
        return false;
    }

    public String next()
    {
        if ( hasNext() )
        {
            // initialized by hasNext()
            final String path = nextPath;
            nextPath = null;
            return path;
        }
        throw new NoSuchElementException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}