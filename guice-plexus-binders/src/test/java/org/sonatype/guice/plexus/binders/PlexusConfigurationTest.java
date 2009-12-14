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

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.guice.plexus.converters.XmlTypeConverter;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.AbstractMatcher;

public class PlexusConfigurationTest
    extends TestCase
{
    @Inject
    ConfiguredComponent component;

    @Inject
    Injector injector;

    static class ComponentWatcher
        extends AbstractMatcher<Class<?>>
        implements BeanWatcher
    {
        static int SEEN;

        public boolean matches( final Class<?> clazz )
        {
            return ConfiguredComponent.class.isAssignableFrom( clazz );
        }

        public void afterInjection( final Object injectee )
        {
            SEEN++;
        }
    }

    @Override
    protected void setUp()
    {
        PlexusGuice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install( new XmlTypeConverter() );
                install( new PlexusBindingModule( new ComponentWatcher(), new AnnotatedPlexusBeanSource( null ) ) );
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

    public void testConfiguration()
    {
        assertEquals( "1", component.a );
        assertEquals( Integer.valueOf( 2 ), component.b );
        assertEquals( 3, component.c );
        assertEquals( Double.valueOf( 4.0 ), component.d );
        assertEquals( 5.0, component.e, 0 );

        assertEquals( 1, ComponentWatcher.SEEN );

        final ConfiguredComponent jitComponent = injector.getInstance( ConfiguredComponent.class );

        assertEquals( "1", jitComponent.a );
        assertEquals( Integer.valueOf( 2 ), jitComponent.b );
        assertEquals( 3, jitComponent.c );
        assertEquals( Double.valueOf( 4.0 ), jitComponent.d );
        assertEquals( 5.0, jitComponent.e, 0 );

        assertEquals( 2, ComponentWatcher.SEEN );
    }
}
