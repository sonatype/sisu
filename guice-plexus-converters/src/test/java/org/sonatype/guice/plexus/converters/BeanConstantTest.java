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
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.sonatype.guice.plexus.config.PlexusTypeConverter;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Jsr330;

public class BeanConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bindBean( final String name, final String clazzName, final String content )
            {
                final String xml = "<bean implementation='" + clazzName + "'>" + content + "</bean>";
                bindConstant().annotatedWith( Jsr330.named( name ) ).to( xml );
            }

            @Override
            protected void configure()
            {
                bindBean( "EmptyBean", EmptyBean.class.getName(), " " );
                bindBean( "MissingType", "some.unknown.type", "" );

                bindBean( "BeanWithProperty", BeanWithProperty.class.getName(), "<value>4.2</value>" );
                bindBean( "MissingProperty", EmptyBean.class.getName(), " <value>4.2</value>" );

                bindBean( "MissingDefaultConstructor", MissingDefaultConstructor.class.getName(), "" );
                bindBean( "BrokenDefaultConstructor", BrokenDefaultConstructor.class.getName(), "" );
                bindBean( "MissingStringConstructor", MissingStringConstructor.class.getName(), "text" );
                bindBean( "BrokenStringConstructor", BrokenStringConstructor.class.getName(), "text" );

                bindConstant().annotatedWith( Jsr330.named( "README" ) ).to( "some/temp/readme.txt" );
                bindConstant().annotatedWith( Jsr330.named( "SITE" ) ).to( "http://www.sonatype.org" );
                bindConstant().annotatedWith( Jsr330.named( "DATE" ) ).to( "2009-11-15 18:02:00" );

                install( new PlexusTypeConverterModule() );
            }
        } ).injectMembers( this );
    }

    static class EmptyBean
    {
    }

    static class BeanWithProperty
    {
        float value;

        void setValue( final float _value )
        {
            value = _value;
        }
    }

    static class MissingDefaultConstructor
    {
        private MissingDefaultConstructor()
        {
        }
    }

    static class BrokenDefaultConstructor
    {
        public BrokenDefaultConstructor()
        {
            throw new RuntimeException();
        }
    }

    static class MissingStringConstructor
    {
    }

    static class BrokenStringConstructor
    {
        @SuppressWarnings( "unused" )
        public BrokenStringConstructor( final String text )
        {
            throw new RuntimeException();
        }
    }

    @Inject
    Injector injector;

    public void testEmptyBeanConversion()
    {
        assertEquals( EmptyBean.class, getBean( "EmptyBean", Object.class ).getClass() );
    }

    public void testMissingType()
    {
        testFailedConversion( "MissingType", EmptyBean.class );
    }

    public void testPeerClassLoader1()
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( null );
            assertEquals( EmptyBean.class, getBean( "EmptyBean", EmptyBean.class ).getClass() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    public void testPeerClassLoader2()
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( null );
            assertEquals( EmptyBean.class, getBean( "EmptyBean", Object.class ).getClass() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    public void testBeanWithPropertyConversion()
    {
        assertEquals( 4.2f, ( (BeanWithProperty) getBean( "BeanWithProperty", Object.class ) ).value, 0f );
    }

    public void testMissingPropertyConversion()
    {
        testFailedConversion( "MissingProperty", Object.class );
    }

    public void testMissingDefaultConstructor()
    {
        testFailedConversion( "MissingDefaultConstructor", Object.class );
    }

    public void testBrokenDefaultConstructor()
    {
        testFailedConversion( "BrokenDefaultConstructor", Object.class );
    }

    public void testMissingStringConstructor()
    {
        testFailedConversion( "MissingStringConstructor", Object.class );
    }

    public void testBrokenStringConstructor()
    {
        testFailedConversion( "BrokenStringConstructor", Object.class );
    }

    public void testSimpleFileBean()
    {
        assertEquals( "readme.txt", injector.getInstance( Key.get( File.class, Jsr330.named( "README" ) ) ).getName() );
    }

    public void testSimpleUrlBean()
    {
        assertEquals( "www.sonatype.org",
                      injector.getInstance( Key.get( URL.class, Jsr330.named( "SITE" ) ) ).getHost() );
    }

    public void testNonBean()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime( injector.getInstance( Key.get( Date.class, Jsr330.named( "DATE" ) ) ) );
        assertEquals( 15, calendar.get( Calendar.DAY_OF_MONTH ) );
        assertEquals( Calendar.NOVEMBER, calendar.get( Calendar.MONTH ) );
        assertEquals( 2009, calendar.get( Calendar.YEAR ) );
    }

    @SuppressWarnings( "boxing" )
    public void testConfigurator()
    {
        final PlexusTypeConverter configurator = injector.getInstance( PlexusTypeConverter.class );
        final float value = configurator.convert( TypeLiteral.get( float.class ), "4.2" );
        assertEquals( 4.2f, value, 0 );
    }

    private Object getBean( final String bindingName, final Class<?> clazz )
    {
        return injector.getInstance( Key.get( clazz, Jsr330.named( bindingName ) ) );
    }

    private void testFailedConversion( final String bindingName, final Class<?> clazz )
    {
        try
        {
            getBean( bindingName, clazz );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
            System.out.println( e );
        }
    }
}
