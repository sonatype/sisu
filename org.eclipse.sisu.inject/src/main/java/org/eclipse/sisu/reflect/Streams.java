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
 *   Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.reflect;

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
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean onWindows;
        try
        {
            onWindows = System.getProperty( "os.name" ).toLowerCase( Locale.US ).contains( "windows" );
        }
        catch ( final RuntimeException e )
        {
            onWindows = false;
        }
        ON_WINDOWS = onWindows;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean ON_WINDOWS;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Streams()
    {
        // static utility class, not allowed to create instances
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
