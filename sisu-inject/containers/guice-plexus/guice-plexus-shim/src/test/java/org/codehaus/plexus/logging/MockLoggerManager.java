package org.codehaus.plexus.logging;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: MockLoggerManager.java 4778 2006-11-23 03:54:18Z jvanzyl $
 */
public class MockLoggerManager
    implements LoggerManager
{
    public void setThreshold( final int threshold )
    {
    }

    public void setThresholds( final int threshold )
    {
    }

    public int getThreshold()
    {
        return 0;
    }

    public void setThreshold( final String role, final int threshold )
    {
    }

    public void setThreshold( final String role, final String roleHint, final int threshold )
    {
    }

    public int getThreshold( final String role )
    {
        return 0;
    }

    public int getThreshold( final String role, final String roleHint )
    {
        return 0;
    }

    public Logger getLoggerForComponent( final String role )
    {
        return new MockLogger( role.getClass().getName() );
    }

    public Logger getLoggerForComponent( final String role, final String roleHint )
    {
        return new MockLogger( role.getClass().getName() + ":" + roleHint );
    }

    public void returnComponentLogger( final String role )
    {
    }

    public void returnComponentLogger( final String role, final String hint )
    {
    }

    public int getActiveLoggerCount()
    {
        return 0;
    }
}
