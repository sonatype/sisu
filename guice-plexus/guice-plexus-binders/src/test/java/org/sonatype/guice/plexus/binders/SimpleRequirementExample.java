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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

public class SimpleRequirementExample
{
    public SimpleRequirementExample()
    {
        final String requirement = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
                install( new PlexusBindingModule( null, new PlexusAnnotatedBeanModule( null, null ) ) );
                bindConstant().annotatedWith( Names.named( "example" ) ).to( "TEST" );
            }
        } ).getInstance( Bean.class ).requirement;

        if ( !requirement.equals( "TEST" ) )
        {
            throw new AssertionError();
        }
    }

    @Component( role = Bean.class )
    static class Bean
    {
        @Requirement( hint = "example" )
        String requirement;
    }
}
