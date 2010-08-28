package org.codehaus.plexus.configuration.xml;

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

import org.codehaus.plexus.configuration.ConfigurationTestHelper;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * @author <a href="mailto:rantene@hotmail.com">Ran Tene</a>
 * @version $Id: XmlPlexusConfigurationTest.java 7187 2008-01-26 20:55:42Z cstamas $
 */
public final class XmlPlexusConfigurationTest
    extends TestCase
{
    private XmlPlexusConfiguration configuration;

    @Override
    public void setUp()
    {
        configuration = new XmlPlexusConfiguration( "a" );
    }

    public void testWithHelper()
        throws Exception
    {
        final PlexusConfiguration c = ConfigurationTestHelper.getTestConfiguration();

        ConfigurationTestHelper.testConfiguration( c );
    }

    public void testGetValue()
        throws Exception
    {
        final String orgValue = "Original String";
        configuration.setValue( orgValue );
        assertEquals( orgValue, configuration.getValue() );
    }

    public void testGetAttribute()
        throws Exception
    {
        final String key = "key";
        final String value = "original value";
        final String defaultStr = "default";
        configuration.setAttribute( key, value );
        assertEquals( value, configuration.getAttribute( key, defaultStr ) );
        assertEquals( defaultStr, configuration.getAttribute( "newKey", defaultStr ) );
    }

    public void testGetChild()
        throws Exception
    {
        PlexusConfiguration child = configuration.getChild( "child" );

        assertNotNull( child );

        child.setValue( "child value" );

        assertEquals( 1, configuration.getChildCount() );

        child = configuration.getChild( "child" );

        assertNotNull( child );

        assertEquals( "child value", child.getValue() );

        assertEquals( 1, configuration.getChildCount() );
    }

    public void testToString()
        throws Exception
    {
        final PlexusConfiguration c = ConfigurationTestHelper.getTestConfiguration();

        assertEquals( "<string string=\"string\">string</string>\n", c.getChild( "string" ).toString() );

        // TODO: uncomment once maven can test the latest plexus-utils
        // assertEquals( "<singleton attribute=\"attribute\"/>\n", c.getChild( "singleton" ).toString() );
    }

    public void testProgrammaticConfigurationCreation()
        throws Exception
    {
        final String viewRoot = "/path/to/viewRoot";

        final PlexusConfiguration c = new XmlPlexusConfiguration( "configuration" ).addChild( "viewRoot", viewRoot );

        assertEquals( viewRoot, c.getChild( "viewRoot" ).getValue() );
    }
}
