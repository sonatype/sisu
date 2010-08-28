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

import java.io.StringReader;

import junit.framework.TestCase;

import org.codehaus.plexus.component.repository.io.PlexusTools;

/**
 * @author Jason van Zyl
 * @version $Id: ConfigurationTestHelper.java 7089 2007-11-25 15:19:06Z jvanzyl $
 */
public abstract class ConfigurationTestHelper
    extends TestCase
{
    public static PlexusConfiguration getTestConfiguration()
        throws Exception
    {
        return PlexusTools.buildConfiguration( "<Test String>",
                                               new StringReader( ConfigurationTestHelper.getXmlConfiguration() ) );
    }

    public static String getXmlConfiguration()
    {
        return "<configuration>" + "<empty-element></empty-element>" + "<singleton attribute='attribute' />"
            + "<string string='string'>string</string>" + "<number number='0'>0</number>"
            + "<not-a-number not-a-number='foo'>not-a-number</not-a-number>"
            + "<boolean-true boolean-true='true'>true</boolean-true>"
            + "<boolean-false boolean-false='false'>false</boolean-false>"
            + "<not-a-boolean>not-a-boolean</not-a-boolean>" + "</configuration>";
    }

    public static void testConfiguration( final PlexusConfiguration c )
        throws Exception
    {
        // Exercise all value/attribute retrieval methods.

        // Values

        // TODO: uncomment once maven can test the latest plexus-utils
        // assertNull( c.getChild( "singleton" ).getValue( null ) );

        // String

        assertEquals( "string", c.getValue( "string" ) );

        assertEquals( "string", c.getChild( "string" ).getValue() );

        assertEquals( "string", c.getChild( "ne-string" ).getValue( "string" ) );

        assertNull( c.getChild( "not-existing" ).getValue( null ) );

        assertEquals( "''", "'" + c.getChild( "empty-element" ).getValue() + "'" );

        assertEquals( "", c.getChild( "empty-element" ).getValue( null ) );

        // Attributes

        assertEquals( "string", c.getChild( "string" ).getAttribute( "string" ) );

        assertEquals( "attribute", c.getChild( "singleton" ).getAttribute( "attribute" ) );
    }
}
