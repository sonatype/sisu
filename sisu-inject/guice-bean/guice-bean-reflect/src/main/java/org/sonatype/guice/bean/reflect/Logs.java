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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for dealing with internal container logs.
 */
public final class Logs
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String ANCHOR = "{}";

    private static final boolean SLF4J_ENABLED;

    private static final boolean DEBUG_ENABLED;

    static
    {
        boolean slf4jEnabled = true;
        boolean debugEnabled = false;
        try
        {
            debugEnabled = org.slf4j.LoggerFactory.getLogger( Logs.class ).isDebugEnabled();
        }
        catch ( final Throwable e )
        {
            slf4jEnabled = false;
            debugEnabled = Logger.getLogger( Logs.class.getName() ).isLoggable( Level.FINE );
        }
        SLF4J_ENABLED = slf4jEnabled;
        DEBUG_ENABLED = debugEnabled;
    }

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Logs()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new Logs(); // keep Cobertura coverage happy
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Formats and logs the given debug message under the given context class
     * 
     * @param clazz The context class
     * @param format The debug message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void debug( final Class<?> clazz, final String format, final Object arg1, final Object arg2 )
    {
        if ( DEBUG_ENABLED )
        {
            if ( SLF4J_ENABLED )
            {
                org.slf4j.LoggerFactory.getLogger( clazz ).debug( format, arg1, arg2 );
            }
            else
            {
                final String at = clazz.getName();
                Logger.getLogger( at ).logp( Level.FINE, at, null, format( format( format, arg1 ), arg2 ) );
            }
        }
    }

    /**
     * Logs the given warning message and cause under the given context class
     * 
     * @param clazz The context class
     * @param message The warning message
     * @param cause The cause
     */
    public static void warn( final Class<?> clazz, final String message, final Throwable cause )
    {
        if ( SLF4J_ENABLED )
        {
            org.slf4j.LoggerFactory.getLogger( clazz ).warn( message, cause );
        }
        else
        {
            final String at = clazz.getName();
            Logger.getLogger( at ).logp( Level.WARNING, at, null, message, cause );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Replaces the first available formatting anchor with the given object.
     * 
     * @param format The format string
     * @param arg The object to format
     */
    private static String format( final String format, final Object arg )
    {
        int cursor = format.indexOf( ANCHOR );
        if ( cursor < 0 )
        {
            return format;
        }
        final StringBuilder buf = new StringBuilder();
        if ( cursor > 0 )
        {
            buf.append( format.substring( 0, cursor ) );
        }
        buf.append( arg );
        cursor += ANCHOR.length();
        if ( cursor < format.length() )
        {
            buf.append( format.substring( cursor, format.length() ) );
        }
        return buf.toString();
    }
}
