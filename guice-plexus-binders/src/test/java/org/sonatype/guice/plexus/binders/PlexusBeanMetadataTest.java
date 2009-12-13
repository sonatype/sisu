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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.StrongClassSpace;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class PlexusBeanMetadataTest
    extends TestCase
{
    @Inject
    @Named( "2" )
    Bean bean;

    @Inject
    Injector injector;

    @Override
    protected void setUp()
    {
        PlexusGuice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindConstant().annotatedWith( Names.named( "KEY1" ) ).to( "REQUIREMENT" );
                install( new PlexusBindingModule( null, new CustomizedBeanSource() ) );
                requestInjection( PlexusBeanMetadataTest.this );
            }
        } );
    }

    interface Bean
    {
        Object getExtraMetadata();

        void setExtraMetadata( Object metadata );
    }

    static class DefaultBean1
        implements Bean
    {
        Object testMetadata;

        public Object getExtraMetadata()
        {
            return testMetadata;
        }

        public void setExtraMetadata( final Object metadata )
        {
            testMetadata = metadata;
        }
    }

    static class DefaultBean2
    {
        String extraMetadata;

        String leftovers;
    }

    static class CustomizedBeanSource
        implements PlexusBeanSource
    {
        public Map<Component, DeferredClass<?>> findPlexusComponentBeans()
        {
            final Map<Component, DeferredClass<?>> componentMap = new HashMap<Component, DeferredClass<?>>();

            componentMap.put( new ComponentImpl( Bean.class, "2", "singleton" ), defer( DefaultBean1.class ) );
            componentMap.put( new ComponentImpl( DefaultBean2.class, "", "per-lookup" ), defer( DefaultBean2.class ) );

            return componentMap;
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            if ( DefaultBean1.class.equals( implementation ) )
            {
                return new PlexusBeanMetadata()
                {
                    public boolean isEmpty()
                    {
                        return false;
                    }

                    public Requirement getRequirement( final BeanProperty<?> property )
                    {
                        if ( "extraMetadata".equals( property.getName() ) )
                        {
                            return new RequirementImpl( String.class, false, "KEY1" );
                        }
                        return null;
                    }

                    public Configuration getConfiguration( final BeanProperty<?> property )
                    {
                        return null;
                    }
                };
            }
            if ( DefaultBean2.class.equals( implementation ) )
            {
                return new PlexusBeanMetadata()
                {
                    private boolean used = false;

                    public boolean isEmpty()
                    {
                        return used;
                    }

                    public Requirement getRequirement( final BeanProperty<?> property )
                    {
                        return null;
                    }

                    public Configuration getConfiguration( final BeanProperty<?> property )
                    {
                        if ( "extraMetadata".equals( property.getName() ) )
                        {
                            used = true;

                            return new ConfigurationImpl( "KEY2", "CONFIGURATION" );
                        }
                        return null;
                    }
                };
            }
            return null;
        }
    }

    public void testExtraMetadata()
    {
        assertEquals( "REQUIREMENT", bean.getExtraMetadata() );
        assertEquals( "CONFIGURATION", injector.getInstance( DefaultBean2.class ).extraMetadata );
        assertSame( bean, injector.getInstance( Key.get( Bean.class, Names.named( "2" ) ) ) );
    }

    static DeferredClass<?> defer( final Class<?> clazz )
    {
        return new StrongClassSpace( TestCase.class.getClassLoader() ).deferLoadClass( clazz.getName() );
    }
}
