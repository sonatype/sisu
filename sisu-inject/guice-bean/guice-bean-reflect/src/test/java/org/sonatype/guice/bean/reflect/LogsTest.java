/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

import java.net.URLClassLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

public class LogsTest
    extends TestCase
{
    public void testLogging()
    {
        new LoggingExample();
    }

    public void testProductionLogging()
        throws Exception
    {
        try
        {
            ( (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( Logs.class ) ).setLevel( ch.qos.logback.classic.Level.WARN );

            final ClassLoader productionLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.startsWith( "ch" ) || name.contains( "cobertura" ) )
                        {
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            productionLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
        }
    }

    public void testFallBackToJDKLogging()
        throws Exception
    {
        final Logger rootLogger = Logger.getLogger( "" );

        final Handler[] handlers = rootLogger.getHandlers();
        if ( handlers.length > 0 )
        {
            handlers[0].setLevel( Level.FINE );
        }

        final Level level = rootLogger.getLevel();
        try
        {
            rootLogger.setLevel( Level.FINE );

            final ClassLoader noSLF4JLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "slf4j" ) )
                        {
                            throw new ClassNotFoundException( name );
                        }
                        if ( name.contains( "cobertura" ) )
                        {
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noSLF4JLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            rootLogger.setLevel( level );
        }
    }
}
