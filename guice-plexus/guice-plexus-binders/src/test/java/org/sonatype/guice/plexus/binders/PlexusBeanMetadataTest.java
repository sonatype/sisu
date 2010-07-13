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
import javax.inject.Named;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
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
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install( new PlexusDateTypeConverter() );

                bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );

                bindConstant().annotatedWith( Names.named( "KEY1" ) ).to( "REQUIREMENT" );

                final PlexusBeanManager manager = new TestBeanManager();

                install( new PlexusBindingModule( null, new BeanSourceA() ) );
                install( new PlexusBindingModule( manager, new BeanSourceB() ) );
                install( new PlexusBindingModule( null, new BeanSourceC() ) );
                install( new PlexusBindingModule( null, new CustomizedBeanSource() ) );

                requestInjection( PlexusBeanMetadataTest.this );
            }
        } );
    }

    static class TestBeanManager
        implements PlexusBeanManager
    {
        public boolean manage( final Class<?> clazz )
        {
            return false;
        }

        public PropertyBinding manage( final BeanProperty<?> property )
        {
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

    interface Bean
    {
        Object getExtraMetadata();

        void setExtraMetadata( Object metadata );
    }

    static class DefaultBean1
        implements Bean
    {
        Object extraMetadata;

        public Object getExtraMetadata()
        {
            return extraMetadata;
        }

        public void setExtraMetadata( final Object metadata )
        {
            extraMetadata = metadata;
        }
    }

    static class DefaultBean2
    {
        String extraMetadata;

        String leftovers;
    }

    static class BeanSourceA
        implements PlexusBeanSource
    {
        public void configure( final Binder binder )
        {
            binder.withSource( "A" ).bind( Bean.class ).annotatedWith( Names.named( "2" ) ).to( DefaultBean1.class ).asEagerSingleton();
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            return null;
        }
    }

    static class BeanSourceB
        implements PlexusBeanSource
    {
        public void configure( final Binder binder )
        {
            binder.withSource( "B" ).bind( DefaultBean2.class );
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            return null;
        }
    }

    static class BeanSourceC
        implements PlexusBeanSource
    {
        public void configure( final Binder binder )
        {
            binder.withSource( "C" ).bind( DefaultBean2.class ).annotatedWith( Names.named( "2" ) ).to( DefaultBean2.class );
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            return null;
        }
    }

    static class CustomizedBeanSource
        implements PlexusBeanSource
    {
        public void configure( final Binder binder )
        {
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
        return new URLClassSpace( TestCase.class.getClassLoader() ).deferLoadClass( clazz.getName() );
    }
}
