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

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class DateConstantTest
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
                bindConstant().annotatedWith( Names.named( name ) ).to( value );
            }

            @Override
            protected void configure()
            {
                bind( "Format1", "2005-10-06 2:22:55.1 PM" );
                bind( "Format2", "2005-10-06 2:22:55PM" );
                bind( "BadFormat", "2005-10-06" );

                install( new DateTypeConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Format1" )
    String dateText1;

    @Inject
    @Named( "Format1" )
    Date date1;

    @Inject
    @Named( "Format2" )
    String dateText2;

    @Inject
    @Named( "Format2" )
    Date date2;

    @Inject
    Injector injector;

    public void testDateFormat1()
    {
        assertEquals( dateText1, new SimpleDateFormat( "yyyy-MM-dd h:mm:ss.S a" ).format( date1 ) );
    }

    public void testDateFormat2()
    {
        assertEquals( dateText2, new SimpleDateFormat( "yyyy-MM-dd h:mm:ssa" ).format( date2 ) );
    }

    public void testBadDateFormat()
    {
        try
        {
            injector.getInstance( Key.get( Date.class, Names.named( "BadFormat" ) ) );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
            System.out.println( e );
        }
    }
}
