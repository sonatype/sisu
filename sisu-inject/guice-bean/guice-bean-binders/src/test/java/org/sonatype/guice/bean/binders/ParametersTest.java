/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.binders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.sonatype.inject.Parameters;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

public class ParametersTest
    extends TestCase
{
    @Inject
    @Parameters
    String[] arguments;

    @Inject
    @Parameters
    Map<String, String> properties;

    public void testDefaultParameters()
    {
        Guice.createInjector( new WireModule( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ParametersTest.class );
            }
        } ) ).injectMembers( this );
        assertEquals( Collections.EMPTY_MAP, properties );
        assertEquals( 0, arguments.length );
    }

    public void testCustomParameters()
    {
        Guice.createInjector( new WireModule( new AbstractModule()
        {
            @Override
            @SuppressWarnings( { "unchecked", "rawtypes" } )
            protected void configure()
            {
                bind( ParametersTest.class );

                bind( ParameterKeys.ARGUMENTS ).toInstance( new String[] { "Hello", "World" } );
                bind( ParameterKeys.PROPERTIES ).toInstance( (Map) System.getProperties() );
            }
        } ) ).injectMembers( this );

        assertEquals( System.getProperties(), properties );
        assertTrue( Arrays.equals( new String[] { "Hello", "World" }, arguments ) );
    }
}
