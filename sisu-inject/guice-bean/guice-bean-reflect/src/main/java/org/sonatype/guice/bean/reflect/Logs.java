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
 * Utility methods for dealing with logs.
 */
public final class Logs
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean DEBUG_ENABLED;

    private static final boolean SLF4J_ENABLED;

    private static final String ANCHOR = "{}";

    static
    {
        boolean debugEnabled = false;
        boolean slf4jEnabled = true;
        try
        {
            debugEnabled = org.slf4j.LoggerFactory.getLogger( Logs.class ).isDebugEnabled();
        }
        catch ( final Throwable e )
        {
            debugEnabled = Logger.getLogger( Logs.class.getName() ).isLoggable( Level.FINE );
            slf4jEnabled = false;
        }
        DEBUG_ENABLED = debugEnabled;
        SLF4J_ENABLED = slf4jEnabled;
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

    public static void debug( final Class<?> clazz, final String fmt, final Object o1, final Object o2 )
    {
        if ( DEBUG_ENABLED )
        {
            if ( SLF4J_ENABLED )
            {
                org.slf4j.LoggerFactory.getLogger( clazz ).debug( fmt, o1, o2 );
            }
            else
            {
                Logger.getLogger( clazz.getName() ).fine( format( format( fmt, o1 ), o2 ) );
            }
        }
    }

    public static void warn( final Class<?> clazz, final String msg, final Throwable e )
    {
        if ( SLF4J_ENABLED )
        {
            org.slf4j.LoggerFactory.getLogger( clazz ).warn( msg, e );
        }
        else
        {
            Logger.getLogger( clazz.getName() ).log( Level.WARNING, msg, e );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static String format( final String fmt, final Object o )
    {
        return fmt.replaceFirst( ANCHOR, String.valueOf( o ) );
    }
}
