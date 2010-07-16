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
package org.codehaus.plexus.component.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentSetDescriptor
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private boolean isolatedRealm;

    private List<ComponentDescriptor<?>> components = Collections.emptyList();

    private List<ComponentDependency> dependencies = Collections.emptyList();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setIsolatedRealm( final boolean isolatedRealm )
    {
        this.isolatedRealm = isolatedRealm;
    }

    public final void addComponentDescriptor( final ComponentDescriptor<?> component )
    {
        if ( components.isEmpty() )
        {
            components = new ArrayList<ComponentDescriptor<?>>();
        }
        components.add( component );
    }

    public final void setComponents( final List<ComponentDescriptor<?>> components )
    {
        this.components = new ArrayList<ComponentDescriptor<?>>( components );
    }

    public final void setDependencies( final List<ComponentDependency> dependencies )
    {
        this.dependencies = new ArrayList<ComponentDependency>( dependencies );
    }

    public final boolean isIsolatedRealm()
    {
        return isolatedRealm;
    }

    public final List<ComponentDescriptor<?>> getComponents()
    {
        return components;
    }

    public final List<ComponentDependency> getDependencies()
    {
        return dependencies;
    }

    @Override
    public final String toString()
    {
        final StringBuffer buf = new StringBuffer( "Component Descriptor: " );
        for ( final ComponentDescriptor<?> cd : components )
        {
            buf.append( cd.getHumanReadableKey() ).append( "\n" );
        }
        return buf.append( "---" ).toString();
    }

    public final void setId( final String id )
    {
    }
}
