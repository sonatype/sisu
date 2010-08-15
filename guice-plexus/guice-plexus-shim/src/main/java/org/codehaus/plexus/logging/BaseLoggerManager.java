/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.logging;

import java.util.Map;
import java.util.WeakHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.guice.plexus.config.Roles;

public abstract class BaseLoggerManager
    extends AbstractLoggerManager
    implements Initializable
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, Logger> activeLoggers = new WeakHashMap<String, Logger>();

    String threshold = "INFO";

    private int currentThreshold;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void initialize()
    {
        currentThreshold = parseThreshold( threshold );
    }

    public final synchronized Logger getLoggerForComponent( final String role, final String hint )
    {
        final String name = Roles.canonicalRoleHint( role, hint );
        Logger logger = activeLoggers.get( name );
        if ( null == logger )
        {
            logger = createLogger( name );
            logger.setThreshold( currentThreshold );
            activeLoggers.put( name, logger );
        }
        return logger;
    }

    public final synchronized void returnComponentLogger( final String role, final String hint )
    {
        activeLoggers.remove( Roles.canonicalRoleHint( role, hint ) );
    }

    public final int getThreshold()
    {
        return currentThreshold;
    }

    public final void setThreshold( final int currentThreshold )
    {
        this.currentThreshold = currentThreshold;
    }

    public final synchronized void setThresholds( final int currentThreshold )
    {
        this.currentThreshold = currentThreshold;
        for ( final Logger logger : activeLoggers.values() )
        {
            logger.setThreshold( currentThreshold );
        }
    }

    public static final int parseThreshold( final String text )
    {
        if ( "DEBUG".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_DEBUG;
        }
        else if ( "INFO".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_INFO;
        }
        else if ( "WARN".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_WARN;
        }
        else if ( "ERROR".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_ERROR;
        }
        else if ( "FATAL".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_FATAL;
        }
        else if ( "DISABLED".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_DISABLED;
        }
        return Logger.LEVEL_DEBUG;
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected abstract Logger createLogger( String name );
}
