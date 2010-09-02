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
 * @author <a href="mailto:peter at realityforge.org">Peter Donald</a>
 * @version $Revision: 4779 $ $Date: 2006-11-23 12:09:31 +0800 (Thu, 23 Nov 2006) $
 */
class MockLogger
    implements Logger
{
    private final String m_name;

    MockLogger( final String name )
    {
        m_name = name;
    }

    public String getName()
    {
        return m_name;
    }

    public Logger getChildLogger( final String name )
    {
        return new MockLogger( getName() + "." + name );
    }

    public void debug( final String message )
    {
    }

    public void debug( final String message, final Throwable throwable )
    {
    }

    public boolean isDebugEnabled()
    {
        return false;
    }

    public void info( final String message )
    {
    }

    public void info( final String message, final Throwable throwable )
    {
    }

    public boolean isInfoEnabled()
    {
        return false;
    }

    public void warn( final String message )
    {
    }

    public void warn( final String message, final Throwable throwable )
    {
    }

    public boolean isWarnEnabled()
    {
        return false;
    }

    public boolean isFatalErrorEnabled()
    {
        return false;
    }

    public void fatalError( final String message )
    {
    }

    public void fatalError( final String message, final Throwable throwable )
    {
    }

    public void error( final String message )
    {
    }

    public void error( final String message, final Throwable throwable )
    {
    }

    public boolean isErrorEnabled()
    {
        return false;
    }

    public int getThreshold()
    {
        return 0;
    }

    public void setThreshold( final int threshold )
    {
    }
}
