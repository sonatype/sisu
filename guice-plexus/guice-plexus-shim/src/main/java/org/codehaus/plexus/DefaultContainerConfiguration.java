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
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentRepository;

public final class DefaultContainerConfiguration
    implements ContainerConfiguration
{
    private String name;

    private String configurationPath;

    private URL configurationUrl;

    private ClassWorld classWorld;

    private ClassRealm classRealm;

    private ComponentRepository repository;

    private Map<Object, Object> contextData;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ContainerConfiguration setName( final String name )
    {
        this.name = name;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public ContainerConfiguration setContainerConfiguration( final String configurationPath )
    {
        this.configurationPath = configurationPath;
        return this;
    }

    public String getContainerConfiguration()
    {
        return configurationPath;
    }

    public ContainerConfiguration setContainerConfigurationURL( final URL configurationUrl )
    {
        this.configurationUrl = configurationUrl;
        return this;
    }

    public URL getContainerConfigurationURL()
    {
        return configurationUrl;
    }

    public ContainerConfiguration setClassWorld( final ClassWorld classWorld )
    {
        this.classWorld = classWorld;
        return this;
    }

    public ClassWorld getClassWorld()
    {
        return classWorld;
    }

    public ContainerConfiguration setRealm( final ClassRealm classRealm )
    {
        this.classRealm = classRealm;
        return this;
    }

    public ClassRealm getRealm()
    {
        return classRealm;
    }

    public ContainerConfiguration setComponentRepository( final ComponentRepository repository )
    {
        this.repository = repository;
        return this;
    }

    public ComponentRepository getComponentRepository()
    {
        return repository;
    }

    public ContainerConfiguration setContext( final Map<Object, Object> contextData )
    {
        this.contextData = contextData;
        return this;
    }

    public Map<Object, Object> getContext()
    {
        return contextData;
    }
}
