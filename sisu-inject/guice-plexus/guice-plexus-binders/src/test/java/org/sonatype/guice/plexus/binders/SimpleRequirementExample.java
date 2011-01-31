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
