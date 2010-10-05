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
package org.codehaus.plexus.personality.plexus.lifecycle.phase;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public final class PlexusContainerLocator
    implements ServiceLocator
{
    private final PlexusContainer container;

    public PlexusContainerLocator( final PlexusContainer container )
    {
        this.container = container;
    }

    public Object lookup( final String role )
        throws ComponentLookupException
    {
        return container.lookup( role );
    }

    public Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        return container.lookup( role, hint );
    }

    public Map<String, Object> lookupMap( final String role )
        throws ComponentLookupException
    {
        return container.lookupMap( role );
    }

    public List<Object> lookupList( final String role )
        throws ComponentLookupException
    {
        return container.lookupList( role );
    }

    public void release( final Object component )
        throws ComponentLifecycleException
    {
        container.release( component );
    }

    public void releaseAll( final Map<String, ?> components )
        throws ComponentLifecycleException
    {
        container.releaseAll( components );
    }

    public void releaseAll( final List<?> components )
        throws ComponentLifecycleException
    {
        container.releaseAll( components );
    }

    public boolean hasComponent( final String role )
    {
        return container.hasComponent( role );
    }

    public boolean hasComponent( final String role, final String hint )
    {
        return container.hasComponent( role, hint );
    }
}
