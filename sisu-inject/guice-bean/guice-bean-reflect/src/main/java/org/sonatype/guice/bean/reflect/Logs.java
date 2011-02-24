/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for dealing with internal debug and warning messages.
 */
public final class Logs
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

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
    // Constants
    // ----------------------------------------------------------------------

    private static final String ANCHOR = "{}";

    private static final boolean SLF4J_ENABLED;

    private static final boolean DEBUG_ENABLED;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Logs()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Formats and logs the given debug message under the given context class; uses "{}" formatting anchors.
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
     * Logs the given warning message and cause under the given context class.
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
