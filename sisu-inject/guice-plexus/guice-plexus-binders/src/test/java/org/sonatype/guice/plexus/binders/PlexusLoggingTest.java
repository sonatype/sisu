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

package org.sonatype.guice.plexus.binders;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

public class PlexusLoggingTest
    extends TestCase
{
    static class LoggerManager
        implements PlexusBeanManager
    {
        public boolean manage( final Class<?> clazz )
        {
            return false;
        }

        @SuppressWarnings( "rawtypes" )
        public PropertyBinding manage( final BeanProperty property )
        {
            if ( Logger.class.equals( property.getType().getRawType() ) )
            {
                return new PropertyBinding()
                {
                    @SuppressWarnings( "unchecked" )
                    public <B> void injectProperty( final B bean )
                    {
                        property.set( bean, LoggerFactory.getLogger( bean.getClass() ) );
                    }
                };
            }
            return null;
        }

        public boolean manage( final Object bean )
        {
            return false;
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

                bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );

                install( new PlexusBindingModule( new LoggerManager(), new PlexusAnnotatedBeanModule( null, null ) ) );

                requestInjection( PlexusLoggingTest.this );
            }
        } );
    }

    @Component( role = Object.class )
    static class SomeComponent
    {
        @Requirement
        Logger logger;
    }

    @Inject
    SomeComponent component;

    public void testLogging()
    {
        assertNotNull( component.logger );

        assertEquals( SomeComponent.class.getName(), component.logger.getName() );
    }
}
