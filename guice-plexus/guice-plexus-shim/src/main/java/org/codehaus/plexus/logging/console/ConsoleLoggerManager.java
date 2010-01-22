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
package org.codehaus.plexus.logging.console;

import org.codehaus.plexus.logging.AbstractLoggerManager;
import org.codehaus.plexus.logging.Logger;

public final class ConsoleLoggerManager
    extends AbstractLoggerManager
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private final static Logger LOGGER = new ConsoleLogger( Logger.LEVEL_INFO, null );

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setThreshold( final String threshold )
    {
        LOGGER.setThreshold( parseThreshold( threshold ) );
    }

    public Logger getLoggerForComponent( final String role, final String hint )
    {
        return LOGGER;
    }

    public void returnComponentLogger( final String role, final String hint )
    {
        // nothing to do
    }

    public int getThreshold()
    {
        return LOGGER.getThreshold();
    }

    public void setThresholds( final int currentThreshold )
    {
        LOGGER.setThreshold( currentThreshold );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static final int parseThreshold( final String text )
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

        return Logger.LEVEL_DISABLED;
    }
}
