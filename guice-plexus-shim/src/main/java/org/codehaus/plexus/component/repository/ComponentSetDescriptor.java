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

    private String source;

    private final List<ComponentDescriptor<?>> components = new ArrayList<ComponentDescriptor<?>>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setId( final String id )
    {
        // TODO: do we need to do anything here?
        System.out.println( "TODO ComponentSetDescriptor.setId(" + id + ")" );
    }

    public final void setSource( final String source )
    {
        this.source = source;
    }

    public final String getSource()
    {
        return source;
    }

    public final void addComponentDescriptor( final ComponentDescriptor<?> component )
    {
        components.add( component );
    }

    public final void addDependency( final ComponentDependency dependency )
    {
        // TODO: do we need to do anything here?
        System.out.println( "TODO ComponentSetDescriptor.addDependency(" + dependency + ")" );
    }

    public final List<ComponentDescriptor<?>> getComponents()
    {
        return Collections.unmodifiableList( components );
    }
}
