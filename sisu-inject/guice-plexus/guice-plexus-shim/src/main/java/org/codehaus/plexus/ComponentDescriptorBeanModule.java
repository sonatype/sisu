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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.factory.ComponentFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.DeferredProvider;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusTypeBinder;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.Binder;
import com.google.inject.ProvisionException;

final class ComponentDescriptorBeanModule
    implements PlexusBeanModule
{
    private final ClassSpace space;

    private final Map<Component, DeferredClass<?>> componentMap = new HashMap<Component, DeferredClass<?>>();

    private final Map<String, PlexusBeanMetadata> metadataMap = new HashMap<String, PlexusBeanMetadata>();

    ComponentDescriptorBeanModule( final ClassSpace space, final List<ComponentDescriptor<?>> descriptors )
    {
        this.space = space;

        for ( int i = 0, size = descriptors.size(); i < size; i++ )
        {
            final ComponentDescriptor<?> cd = descriptors.get( i );
            final Component component = newComponent( cd );
            final String factory = cd.getComponentFactory();
            if ( null == factory || "java".equals( factory ) )
            {
                try
                {
                    componentMap.put( component, new LoadedClass<Object>( cd.getImplementationClass() ) );
                }
                catch ( final Throwable e )
                {
                    componentMap.put( component, space.deferLoadClass( cd.getImplementation() ) );
                }
            }
            else
            {
                componentMap.put( component, new DeferredFactoryClass( cd, factory ) );
            }
            final List<ComponentRequirement> requirements = cd.getRequirements();
            if ( !requirements.isEmpty() )
            {
                metadataMap.put( cd.getImplementation(), new ComponentMetadata( space, requirements ) );
            }
        }
    }

    public PlexusBeanSource configure( final Binder binder )
    {
        final PlexusTypeBinder plexusTypeBinder = new PlexusTypeBinder( binder );
        for ( final Entry<Component, DeferredClass<?>> entry : componentMap.entrySet() )
        {
            plexusTypeBinder.hear( entry.getKey(), entry.getValue(), space );
        }
        return new PlexusDescriptorBeanSource( metadataMap );
    }

    static Component newComponent( final ComponentDescriptor<?> cd )
    {
        return new ComponentImpl( cd.getRoleClass(), cd.getRoleHint(), cd.getInstantiationStrategy(),
                                  cd.getDescription() );
    }

    static Requirement newRequirement( final ClassSpace space, final ComponentRequirement cr )
    {
        return new RequirementImpl( space.deferLoadClass( cr.getRole() ), false,
                                    Collections.singletonList( cr.getRoleHint() ) );
    }

    private static final class DeferredFactoryClass
        implements DeferredClass<Object>, DeferredProvider<Object>
    {
        @Inject
        private PlexusContainer container;

        @Inject
        private PlexusBeanManager manager;

        private final ComponentDescriptor<?> cd;

        private final String hint;

        DeferredFactoryClass( final ComponentDescriptor<?> cd, final String hint )
        {
            this.cd = cd;
            this.hint = hint;
        }

        @SuppressWarnings( { "unchecked", "rawtypes" } )
        public Class load()
            throws TypeNotPresentException
        {
            return cd.getImplementationClass();
        }

        public String getName()
        {
            return cd.getImplementation();
        }

        public DeferredProvider<Object> asProvider()
        {
            return this;
        }

        public Object get()
        {
            try
            {
                final ComponentFactory factory = container.lookup( ComponentFactory.class, hint );
                final Object o = factory.newInstance( cd, container.getLookupRealm(), container );
                if ( null != o && manager.manage( o.getClass() ) )
                {
                    manager.manage( o );
                }
                return o;
            }
            catch ( final Throwable e )
            {
                throw new ProvisionException( "Error in ComponentFactory:" + hint, e );
            }
        }

        public DeferredClass<Object> getImplementationClass()
        {
            return this;
        }
    }

    private static final class ComponentMetadata
        implements PlexusBeanMetadata
    {
        private Map<String, Requirement> requirementMap = new HashMap<String, Requirement>();

        ComponentMetadata( final ClassSpace space, final List<ComponentRequirement> requirements )
        {
            for ( int i = 0, size = requirements.size(); i < size; i++ )
            {
                final ComponentRequirement cr = requirements.get( i );
                requirementMap.put( cr.getFieldName(), newRequirement( space, cr ) );
            }
        }

        public boolean isEmpty()
        {
            return requirementMap.isEmpty();
        }

        public Requirement getRequirement( final BeanProperty<?> property )
        {
            final Requirement requirement = requirementMap.get( property.getName() );
            if ( null != requirement && requirementMap.isEmpty() )
            {
                requirementMap = Collections.emptyMap();
            }
            return requirement;
        }

        public Configuration getConfiguration( final BeanProperty<?> property )
        {
            return null;
        }
    }

    private static final class PlexusDescriptorBeanSource
        implements PlexusBeanSource
    {
        private Map<String, PlexusBeanMetadata> metadataMap;

        PlexusDescriptorBeanSource( final Map<String, PlexusBeanMetadata> metadataMap )
        {
            this.metadataMap = metadataMap;
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            if ( null == metadataMap )
            {
                return null;
            }
            final PlexusBeanMetadata metadata = metadataMap.remove( implementation.getName() );
            if ( metadataMap.isEmpty() )
            {
                metadataMap = null;
            }
            return metadata;
        }
    }
}
