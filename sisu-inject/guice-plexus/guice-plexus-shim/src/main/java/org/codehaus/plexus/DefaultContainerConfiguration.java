/**
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

public final class DefaultContainerConfiguration
    implements ContainerConfiguration
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String configurationPath;

    private URL configurationUrl;

    private ClassWorld classWorld;

    private ClassRealm classRealm;

    private Map<Object, Object> contextData;

    private String componentVisibility = PlexusConstants.REALM_VISIBILITY;

    private boolean autoWiring;

    private boolean classPathScanning;

    private boolean classPathCaching;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ContainerConfiguration setName( final String name )
    {
        return this;
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

    public ContainerConfiguration setContext( final Map<Object, Object> contextData )
    {
        this.contextData = contextData;
        return this;
    }

    public Map<Object, Object> getContext()
    {
        return contextData;
    }

    public ContainerConfiguration setComponentVisibility( final String componentVisibility )
    {
        this.componentVisibility = componentVisibility;
        return this;
    }

    public String getComponentVisibility()
    {
        return componentVisibility;
    }

    public ContainerConfiguration setAutoWiring( final boolean autoWiring )
    {
        this.autoWiring = autoWiring;
        return this;
    }

    public boolean getAutoWiring()
    {
        return autoWiring;
    }

    public ContainerConfiguration setClassPathScanning( final boolean classPathScanning )
    {
        this.classPathScanning = classPathScanning;
        if ( classPathScanning )
        {
            autoWiring = true;
        }
        return this;
    }

    public boolean getClassPathScanning()
    {
        return classPathScanning;
    }

    public ContainerConfiguration setClassPathCaching( final boolean classPathCaching )
    {
        this.classPathCaching = classPathCaching;
        return this;
    }

    public boolean getClassPathCaching()
    {
        return classPathCaching;
    }
}
