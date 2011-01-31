/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
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
