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

public abstract class AbstractLogger
    implements Logger
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private int threshold;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public AbstractLogger( final int threshold, @SuppressWarnings( "unused" ) final String name )
    {
        if ( threshold < LEVEL_DEBUG || LEVEL_DISABLED < threshold )
        {
            throw new IllegalArgumentException( "Threshold " + threshold + " is not valid" );
        }

        this.threshold = threshold;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void debug( final String message )
    {
        debug( message, null );
    }

    public boolean isDebugEnabled()
    {
        return threshold <= LEVEL_DEBUG;
    }

    public final void info( final String message )
    {
        info( message, null );
    }

    public boolean isInfoEnabled()
    {
        return threshold <= LEVEL_INFO;
    }

    public final void warn( final String message )
    {
        warn( message, null );
    }

    public boolean isWarnEnabled()
    {
        return threshold <= LEVEL_WARN;
    }

    public final void error( final String message )
    {
        error( message, null );
    }

    public boolean isErrorEnabled()
    {
        return threshold <= LEVEL_ERROR;
    }

    public final void fatalError( final String message )
    {
        fatalError( message, null );
    }

    public boolean isFatalErrorEnabled()
    {
        return threshold <= LEVEL_FATAL;
    }

    public final void setThreshold( final int threshold )
    {
        this.threshold = threshold;
    }
}
