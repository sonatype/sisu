package org.codehaus.plexus.logging.console;

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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Jason van Zyl
 * @version $Id: ConsoleLoggerTest.java 7089 2007-11-25 15:19:06Z jvanzyl $
 */
public class ConsoleLoggerTest
    extends TestCase
{
    public void testConsoleLogger()
    {
        final ConsoleLogger logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "test" );

        assertTrue( logger.isDebugEnabled() );

        assertTrue( logger.isInfoEnabled() );

        assertTrue( logger.isWarnEnabled() );

        assertTrue( logger.isErrorEnabled() );

        assertTrue( logger.isFatalErrorEnabled() );

        // Save the original print stream.
        final PrintStream original = System.out;

        final Throwable t = new Throwable( "throwable" );

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PrintStream consoleStream = new PrintStream( os );

        System.setOut( consoleStream );

        logger.debug( "debug" );

        assertEquals( "[DEBUG] debug", getMessage( consoleStream, os ) );

        logger.debug( "debug", t );

        assertEquals( "[DEBUG] debug", getMessage( consoleStream, os ) );

        os = new ByteArrayOutputStream();

        consoleStream = new PrintStream( os );

        System.setOut( consoleStream );

        logger.info( "info" );

        assertEquals( "[INFO] info", getMessage( consoleStream, os ) );

        logger.info( "info", t );

        assertEquals( "[INFO] info", getMessage( consoleStream, os ) );

        os = new ByteArrayOutputStream();

        consoleStream = new PrintStream( os );

        System.setOut( consoleStream );

        logger.warn( "warn" );

        assertEquals( "[WARNING] warn", getMessage( consoleStream, os ) );

        logger.warn( "warn", t );

        assertEquals( "[WARNING] warn", getMessage( consoleStream, os ) );

        os = new ByteArrayOutputStream();

        consoleStream = new PrintStream( os );

        System.setOut( consoleStream );

        logger.error( "error" );

        assertEquals( "[ERROR] error", getMessage( consoleStream, os ) );

        logger.error( "error", t );

        assertEquals( "[ERROR] error", getMessage( consoleStream, os ) );

        os = new ByteArrayOutputStream();

        consoleStream = new PrintStream( os );

        System.setOut( consoleStream );

        logger.fatalError( "error" );

        assertEquals( "[FATAL ERROR] error", getMessage( consoleStream, os ) );

        logger.fatalError( "error", t );

        assertEquals( "[FATAL ERROR] error", getMessage( consoleStream, os ) );

        // Set the original print stream.
        System.setOut( original );
    }

    private String getMessage( final PrintStream consoleStream, final ByteArrayOutputStream os )
    {
        consoleStream.flush();

        consoleStream.close();

        return StringUtils.chopNewline( os.toString() );
    }
}
