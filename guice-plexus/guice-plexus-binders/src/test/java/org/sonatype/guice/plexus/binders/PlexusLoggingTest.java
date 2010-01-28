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
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.GuiceBeanLocator;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

public class PlexusLoggingTest
    extends TestCase
{
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

                install( new PlexusBindingModule( null, new AnnotatedPlexusBeanSource( null, null ) ) );

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
