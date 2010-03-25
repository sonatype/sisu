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
package org.sonatype.guice.plexus.binders;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.GuiceBeanLocator;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

public class PlexusConfigurationTest
    extends TestCase
{
    @Inject
    ConfiguredComponent component;

    @Inject
    Injector injector;

    static class ComponentManager
        implements PlexusBeanManager
    {
        static int SEEN;

        public boolean manage( final Component component, final DeferredClass<?> clazz )
        {
            return true;
        }

        public boolean manage( final Class<?> clazz )
        {
            return ConfiguredComponent.class.isAssignableFrom( clazz );
        }

        public PropertyBinding manage( final BeanProperty<?> property )
        {
            return null;
        }

        public boolean manage( final Object bean )
        {
            SEEN++;
            return true;
        }

        public PlexusBeanManager manageChild()
        {
            return this;
        }

        public boolean unmanage( final Object bean )
        {
            return false;
        }

        public boolean unmanage()
        {
            return false;
        }
    }

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install( new PlexusDateTypeConverter() );
                install( new PlexusXmlBeanConverter() );

                bind( PlexusBeanLocator.class ).to( GuiceBeanLocator.class );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );

                install( new PlexusBindingModule( new ComponentManager(), new AnnotatedPlexusBeanSource( null, null ) ) );

                requestInjection( PlexusConfigurationTest.this );
            }
        } );
    }

    @Component( role = Object.class )
    static class ConfiguredComponent
    {
        @Configuration( "1" )
        String a;

        @Configuration( "2" )
        Integer b;

        @Configuration( "3" )
        int c;

        @Configuration( "4" )
        Double d;

        @Configuration( "5" )
        double e;
    }

    @Component( role = Object.class )
    static class MisconfiguredComponent
    {
        @Configuration( "misconfigured" )
        SomeBean bean;
    }

    public static class SomeBean
    {
        public SomeBean( final String data )
        {
            if ( "misconfigured".equals( data ) )
            {
                throw new NoClassDefFoundError();
            }
        }
    }

    public void testConfiguration()
    {
        assertEquals( "1", component.a );
        assertEquals( Integer.valueOf( 2 ), component.b );
        assertEquals( 3, component.c );
        assertEquals( Double.valueOf( 4.0 ), component.d );
        assertEquals( 5.0, component.e, 0 );

        assertEquals( 1, ComponentManager.SEEN );

        final ConfiguredComponent jitComponent = injector.getInstance( ConfiguredComponent.class );

        assertEquals( "1", jitComponent.a );
        assertEquals( Integer.valueOf( 2 ), jitComponent.b );
        assertEquals( 3, jitComponent.c );
        assertEquals( Double.valueOf( 4.0 ), jitComponent.d );
        assertEquals( 5.0, jitComponent.e, 0 );

        assertEquals( 2, ComponentManager.SEEN );

        try
        {
            injector.getInstance( MisconfiguredComponent.class );
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        assertEquals( 2, ComponentManager.SEEN );
    }
}
