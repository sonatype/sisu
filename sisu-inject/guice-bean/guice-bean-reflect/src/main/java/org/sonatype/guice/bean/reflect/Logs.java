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
        boolean debugEnabled = false;
        boolean slf4jEnabled = false;
        final Object[] logger = { null };
        final String name = "Sisu";
        try
        {
            logger[0] = org.slf4j.LoggerFactory.getLogger( name );
            debugEnabled = ( (org.slf4j.Logger) logger[0] ).isDebugEnabled();
            slf4jEnabled = true;
        }
        catch ( final Throwable e )
        {
            logger[0] = Logger.getLogger( name );
            debugEnabled = ( (Logger) logger[0] ).isLoggable( Level.FINE );
        }
        DEBUG_ENABLED = debugEnabled;
        SLF4J_ENABLED = slf4jEnabled;
        LOGGER = logger[0];
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String ANCHOR = "{}";

    private static final boolean DEBUG_ENABLED;

    private static final boolean SLF4J_ENABLED;

    private static final Object LOGGER;

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
     * Formats and logs the given debug message; uses "{}" formatting anchors.<br>
     * Note: pass {@link Throwable} values in the final parameter for special handling.
     * 
     * @param format The debug message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void debug( final String format, final Object arg1, final Object arg2 )
    {
        if ( DEBUG_ENABLED )
        {
            final String message = format( format( format, arg1 ), arg2 );
            if ( arg2 instanceof Throwable )
            {
                if ( SLF4J_ENABLED )
                {
                    ( (org.slf4j.Logger) LOGGER ).debug( message, (Throwable) arg2 );
                }
                else
                {
                    ( (Logger) LOGGER ).log( Level.FINE, message, (Throwable) arg2 );
                }
            }
            else
            {
                if ( SLF4J_ENABLED )
                {
                    ( (org.slf4j.Logger) LOGGER ).debug( message );
                }
                else
                {
                    ( (Logger) LOGGER ).fine( message );
                }
            }
        }
    }

    /**
     * Formats and logs the given warning message; uses "{}" formatting anchors.<br>
     * Note: pass {@link Throwable} values in the final parameter for special handling.
     * 
     * @param format The warning message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void warn( final String format, final Object arg1, final Object arg2 )
    {
        final String message = format( format( format, arg1 ), arg2 );
        if ( arg2 instanceof Throwable )
        {
            if ( SLF4J_ENABLED )
            {
                ( (org.slf4j.Logger) LOGGER ).warn( message, (Throwable) arg2 );
            }
            else
            {
                ( (Logger) LOGGER ).log( Level.WARNING, message, (Throwable) arg2 );
            }
        }
        else
        {
            if ( SLF4J_ENABLED )
            {
                ( (org.slf4j.Logger) LOGGER ).warn( message );
            }
            else
            {
                ( (Logger) LOGGER ).warning( message );
            }
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
        try
        {
            buf.append( arg );
        }
        catch ( final Throwable e )
        {
            buf.append( null != arg ? arg.getClass() : null );
        }
        cursor += ANCHOR.length();
        if ( cursor < format.length() )
        {
            buf.append( format.substring( cursor, format.length() ) );
        }
        return buf.toString();
    }
}
