package org.codehaus.plexus;

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

import junit.framework.TestCase;
import org.codehaus.plexus.component.discovery.DiscoveredComponent;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.test.DefaultLoadOnStartService;

import java.io.File;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: PlexusTestCaseTest.java 7828 2008-11-14 22:07:56Z dain $
 */
public class PlexusTestCaseTest
    extends TestCase
{
    private String basedir;

    public void setUp()
    {
        basedir = System.getProperty( "basedir" );

        if ( basedir == null )
        {
            basedir = new File( "." ).getAbsolutePath();
        }
    }

    public void testPlexusTestCase()
        throws Exception
    {
        PlexusTestCase tc = new PlexusTestCase()
        {
        };

        tc.setUp();

        try
        {
            tc.lookup( DiscoveredComponent.class, "unknown" );

            fail( "Expected ComponentLookupException." );
        }
        catch ( ComponentLookupException ex )
        {
            assertTrue( true );
        }

        // This component is discovered from src/test/META-INF/plexus/components.xml
        DiscoveredComponent component = tc.lookup( DiscoveredComponent.class );

        assertNotNull( component );

        assertNotNull( tc.getClassLoader() );

        tc.tearDown();
    }

    public void testLoadOnStartComponents()
        throws Exception
    {
        PlexusTestCase tc = new PlexusTestCase()
        {
            protected String getCustomConfigurationName()
            {
                return PlexusTestCase.getTestConfiguration( getClass() );
            }
        };

        tc.setupContainer();

        // Assert that the load on start component has started.

        assertTrue( "The load on start components haven't been started.", DefaultLoadOnStartService.isStarted );

        tc.tearDown();
    }

    public void testGetFile()
    {
        File file = PlexusTestCase.getTestFile( "pom.xml" );

        assertTrue( file.exists() );

        file = PlexusTestCase.getTestFile( basedir, "pom.xml" );

        assertTrue( file.exists() );
    }

    public void testGetPath()
    {
        File file = new File( PlexusTestCase.getTestPath( "pom.xml" ) );

        assertTrue( file.exists() );

        file = new File( PlexusTestCase.getTestPath( basedir, "pom.xml" ) );

        assertTrue( file.exists() );
    }
}
