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
package org.sonatype.guice.bean.converters;

import java.net.URL;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class URLTypeConverterTest
    extends TestCase
{
    public void testURLConversion()
    {
        final URL url = Guice.createInjector( new URLTypeConverter(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindConstant().annotatedWith( Names.named( "url" ) ).to( "http://127.0.0.1/" );
            }
        } ).getInstance( Key.get( URL.class, Names.named( "url" ) ) );

        assertEquals( "http://127.0.0.1/", url.toString() );
    }

    public void testBrokenURLConversion()
    {
        try
        {
            Guice.createInjector( new URLTypeConverter(), new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bindConstant().annotatedWith( Names.named( "url" ) ).to( "foo^bar" );
                }
            } ).getInstance( Key.get( URL.class, Names.named( "url" ) ) );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
        }
    }
}
