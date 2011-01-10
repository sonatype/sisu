/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * Utility methods for dealing with streams.
 */
public final class Streams
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean ON_WINDOWS =
        System.getProperty( "os.name" ).toLowerCase( Locale.US ).contains( "windows" );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Streams()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new Streams(); // keep Cobertura coverage happy
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Opens an input stream to the given URL; disables JAR caching on Windows.
     */
    public static InputStream open( final URL url )
        throws IOException
    {
        if ( ON_WINDOWS )
        {
            final URLConnection conn = url.openConnection();
            conn.setUseCaches( false );
            return conn.getInputStream();
        }
        return url.openStream();
    }
}
