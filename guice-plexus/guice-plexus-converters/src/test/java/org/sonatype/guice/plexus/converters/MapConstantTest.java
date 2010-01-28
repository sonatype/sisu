/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.converters;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Named;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.util.Jsr330;

public class MapConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bind( final String name, final String value )
            {
                bindConstant().annotatedWith( Jsr330.named( name ) ).to( value );
            }

            @Override
            protected void configure()
            {
                bind( "Empty", "<items/>" );

                bind( "Custom", "<items implementation='java.util.LinkedHashMap'>"
                    + "<builder implementation='java.lang.StringBuilder'>TEST</builder>"
                    + "<file implementation='java.io.File'>TEST</file>" + "</items>" );

                bind( "Map", "<items><key1>value1</key1><key2>value2</key2></items>" );

                bind( "Properties", "<items><property><name>key1</name><value>value1</value></property>"
                    + "<property><value>value2</value><name>key2</name></property></items>" );

                install( new PlexusXmlBeanConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Empty" )
    Map<?, ?> empty;

    @Inject
    @Named( "Custom" )
    Map<?, ?> custom;

    @Inject
    @Named( "Map" )
    Map<?, ?> map;

    @Inject
    @Named( "Properties" )
    Properties properties;

    public void testEmptyMap()
    {
        assertTrue( empty.isEmpty() );
    }

    public void testCustomMap()
    {
        assertEquals( LinkedHashMap.class, custom.getClass() );
        assertEquals( "TEST", custom.get( "builder" ).toString() );
        assertEquals( StringBuilder.class, custom.get( "builder" ).getClass() );
        assertEquals( new File( "TEST" ), custom.get( "file" ) );
    }

    public void testMapAndProperties()
    {
        final HashMap<String, String> testMap = new HashMap<String, String>();
        testMap.put( "key1", "value1" );
        testMap.put( "key2", "value2" );

        assertEquals( testMap, map );
        assertEquals( testMap, properties );
    }
}
