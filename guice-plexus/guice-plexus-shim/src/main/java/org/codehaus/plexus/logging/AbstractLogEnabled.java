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

public abstract class AbstractLogEnabled
    implements LogEnabled
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private Logger logger;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void enableLogging( final Logger theLogger )
    {
        logger = theLogger;
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    protected Logger getLogger()
    {
        return logger;
    }

    protected final void setupLogger( final Object component )
    {
        setupLogger( component, logger );
    }

    protected final void setupLogger( final Object component, final String category )
    {
        if ( category == null )
        {
            throw new IllegalStateException( "Logging category must be defined." );
        }
        setupLogger( component, logger.getChildLogger( category ) );
    }

    protected final void setupLogger( final Object component, final Logger logger )
    {
        if ( component instanceof LogEnabled )
        {
            ( (LogEnabled) component ).enableLogging( logger );
        }
    }
}
