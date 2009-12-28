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

import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.jar.JarInputStream;

final class JarEntryIterator
    implements Iterator<String>
{
    private JarInputStream in;

    private String nextPath;

    public JarEntryIterator( final URL url )
    {
        try
        {
            in = new JarInputStream( url.openStream(), false );
            nextPath = "META-INF/MANIFEST.MF";
        }
        catch ( final Exception e ) // NOPMD
        {
            // empty-iterator
        }
    }

    public boolean hasNext()
    {
        if ( null != nextPath )
        {
            return true; // already cached
        }
        try
        {
            // populate cache with the next element
            nextPath = in.getNextJarEntry().getName();
            if ( null != nextPath )
            {
                return true;
            }
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
            // clear cache after using it
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