package org.codehaus.plexus.configuration;

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

/**
 * @author <a href="mailto:rantene@hotmail.com">Ran Tene</a>
 * @version $Id: DefaultPlexusConfigurationTest.java 7854 2008-11-18 22:33:53Z bentmann $
 */
public final class DefaultPlexusConfigurationTest
    extends TestCase
{
    private DefaultPlexusConfiguration configuration;

    @Override
    public void setUp()
    {
        configuration = new DefaultPlexusConfiguration( "a" );
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
        DefaultPlexusConfiguration child = (DefaultPlexusConfiguration) configuration.getChild( "child" );

        assertNotNull( child );

        child.setValue( "child value" );

        assertEquals( 1, configuration.getChildCount() );

        child = (DefaultPlexusConfiguration) configuration.getChild( "child" );

        assertNotNull( child );

        assertEquals( "child value", child.getValue() );

        assertEquals( 1, configuration.getChildCount() );
    }

    public void testToString()
        throws Exception
    {
        // TODO: this currently works since getTestConfiguration() invokes PlexusTools.buildConfiguration()
        // and it returns XmlPlexusConfiguration actually.
        final PlexusConfiguration c = ConfigurationTestHelper.getTestConfiguration();

        assertEquals( "<string string=\"string\">string</string>\n", c.getChild( "string" ).toString() );

        // TODO: uncomment once maven can test the latest plexus-utils
        assertEquals( "<singleton attribute=\"attribute\"/>\n", c.getChild( "singleton" ).toString() );
    }

    public void testProgrammaticConfigurationCreation()
        throws Exception
    {
        final String viewRoot = "/path/to/viewRoot";

        final PlexusConfiguration c = new DefaultPlexusConfiguration( "configuration" ).addChild( "viewRoot", viewRoot );

        assertEquals( viewRoot, c.getChild( "viewRoot" ).getValue() );
    }

    public void testChildOrdering()
        throws Exception
    {
        final PlexusConfiguration child0 = new DefaultPlexusConfiguration( "child" );
        final PlexusConfiguration child1 = new DefaultPlexusConfiguration( "child" );
        final PlexusConfiguration child2 = new DefaultPlexusConfiguration( "special-child" );
        final PlexusConfiguration child3 = new DefaultPlexusConfiguration( "child" );
        final PlexusConfiguration child4 = new DefaultPlexusConfiguration( "child" );

        configuration.addChild( child0 );
        configuration.addChild( child1 );
        configuration.addChild( child2 );
        configuration.addChild( child3 );
        configuration.addChild( child4 );

        assertEquals( 5, configuration.getChildCount() );
        assertSame( child0, configuration.getChild( 0 ) );
        assertSame( child1, configuration.getChild( 1 ) );
        assertSame( child2, configuration.getChild( 2 ) );
        assertSame( child3, configuration.getChild( 3 ) );
        assertSame( child4, configuration.getChild( 4 ) );
    }

}
