/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.codehaus.plexus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.DeferredProvider;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.scanners.AbstractAnnotatedPlexusBeanSource;

import com.google.inject.Injector;
import com.google.inject.Provider;

final class ComponentDescriptorBeanSource
    extends AbstractAnnotatedPlexusBeanSource
{
    // ------------------------
    // TODO: work in progress !
    // ------------------------

    private final ComponentDescriptor<?>[] descriptors;

    ComponentDescriptorBeanSource( final Map<?, ?> variables, final Collection<ComponentDescriptor<?>> descriptors )
    {
        super( variables );

        this.descriptors = descriptors.toArray( new ComponentDescriptor[descriptors.size()] );
    }

    public Map<Component, DeferredClass<?>> findPlexusComponentBeans()
    {
        final Map<Component, DeferredClass<?>> beans = new HashMap<Component, DeferredClass<?>>();
        for ( final ComponentDescriptor<?> descriptor : descriptors )
        {
            final Component component =
                new ComponentImpl( descriptor.getRoleClass(), descriptor.getRoleHint(),
                                   descriptor.getInstantiationStrategy(), descriptor.getDescription() );

            beans.put( component, new DeferredDescriptorClass( descriptor ) );
        }
        return beans;
    }

    @Override
    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        for ( final ComponentDescriptor<?> descriptor : descriptors )
        {
            if ( implementation.getName().equals( descriptor.getImplementation() ) )
            {
                if ( !descriptor.getRequirements().isEmpty() )
                {
                    return new DescriptorBeanMetadata( descriptor );
                }
                break;
            }
        }
        return this;
    }

    private static class DeferredDescriptorClass
        implements DeferredClass<Object>, DeferredProvider<Object>
    {
        private final ComponentDescriptor<?> descriptor;

        @Inject
        private Injector injector;

        DeferredDescriptorClass( final ComponentDescriptor<?> descriptor )
        {
            this.descriptor = descriptor;
        }

        public String getName()
        {
            return descriptor.getImplementation();
        }

        @SuppressWarnings( "unchecked" )
        public Class load()
        {
            return descriptor.getImplementationClass();
        }

        public Provider<Object> asProvider()
        {
            return this;
        }

        public DeferredClass<Object> getImplementationClass()
        {
            return this;
        }

        @SuppressWarnings( "unchecked" )
        public Object get()
        {
            return injector.getInstance( load() );
        }

        @Override
        public String toString()
        {
            return getName();
        }
    }

    private class DescriptorBeanMetadata
        implements PlexusBeanMetadata
    {
        private final List<ComponentRequirement> requirements;

        private final ClassRealm realm;

        DescriptorBeanMetadata( final ComponentDescriptor<?> descriptor )
        {
            requirements = descriptor.getRequirements();
            realm = descriptor.getRealm();
        }

        public boolean isEmpty()
        {
            return false;
        }

        public Configuration getConfiguration( final BeanProperty<?> property )
        {
            return ComponentDescriptorBeanSource.this.getConfiguration( property );
        }

        public Requirement getRequirement( final BeanProperty<?> property )
        {
            for ( int i = 0, length = requirements.size(); i < length; i++ )
            {
                final ComponentRequirement requirement = requirements.get( i );
                if ( property.getName().equals( requirement.getFieldName() ) )
                {
                    try
                    {
                        final Class<?> role = realm.loadClass( requirement.getRole() );
                        return new RequirementImpl( role, false, requirement.getRoleHint() );
                    }
                    catch ( final Throwable t )
                    {
                        break;
                    }
                }
            }
            return ComponentDescriptorBeanSource.this.getRequirement( property );
        }
    }
}
