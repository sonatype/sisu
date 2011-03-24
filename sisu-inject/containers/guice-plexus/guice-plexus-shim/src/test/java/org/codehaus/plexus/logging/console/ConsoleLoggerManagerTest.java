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

import org.codehaus.plexus.logging.AbstractLoggerManagerTest;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

/**
 * Test for {@link org.codehaus.plexus.logging.console.ConsoleLoggerManager} and
 * {@link org.codehaus.plexus.logging.console.ConsoleLogger}.
 * 
 * @author Mark H. Wilkinson
 * @version $Revision: 7828 $
 */
public final class ConsoleLoggerManagerTest
    extends AbstractLoggerManagerTest
{
    @Override
    protected LoggerManager createLoggerManager()
        throws Exception
    {
        return lookup( LoggerManager.class );
    }

    @Override
    public void testSetThreshold()
    {
        // disable test
    }

    @Override
    public void testActiveLoggerCount()
    {
        // disable test
    }

    public void testSetAllThresholds()
        throws Exception
    {
        final LoggerManager manager = createLoggerManager();
        manager.setThreshold( Logger.LEVEL_ERROR );

        final Logger logger = manager.getLoggerForComponent( "test" );
        assertEquals( logger.getThreshold(), Logger.LEVEL_ERROR );

        manager.setThresholds( Logger.LEVEL_DEBUG );
        assertEquals( logger.getThreshold(), Logger.LEVEL_DEBUG );
    }
}
