/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.reflect.oops;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler
    extends URLStreamHandler
{
    @Override
    protected URLConnection openConnection( final URL url )
        throws IOException
    {
        return new BadConnection( url );
    }

    static class BadConnection
        extends URLConnection
    {
        BadConnection( final URL url )
        {
            super( url );
        }

        @Override
        public void connect()
            throws IOException
        {
        }

        @Override
        public InputStream getInputStream()
            throws IOException
        {
            final InputStream in = new ByteArrayInputStream( new byte[0] );
            return new InputStream()
            {
                @Override
                public int read()
                    throws IOException
                {
                    return in.read();
                }

                @Override
                public void close()
                    throws IOException
                {
                    throw new IOException();
                }
            };
        }
    }
}
