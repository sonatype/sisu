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
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.logging.Logger;

public final class ConsoleLoggerManager
    extends AbstractLoggerManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Logger logger = new ConsoleLogger( Logger.LEVEL_INFO, "console" );

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setThreshold( final String threshold )
    {
        logger.setThreshold( BaseLoggerManager.parseThreshold( threshold ) );
    }

    public Logger getLoggerForComponent( final String role, final String hint )
    {
        return logger;
    }

    public void returnComponentLogger( final String role, final String hint )
    {
        // nothing to do
    }

    public int getThreshold()
    {
        return logger.getThreshold();
    }

    public void setThreshold( final int currentThreshold )
    {
        logger.setThreshold( currentThreshold );
    }

    public void setThresholds( final int currentThreshold )
    {
        logger.setThreshold( currentThreshold );
    }

    public int getActiveLoggerCount()
    {
        return 0;
    }
}
