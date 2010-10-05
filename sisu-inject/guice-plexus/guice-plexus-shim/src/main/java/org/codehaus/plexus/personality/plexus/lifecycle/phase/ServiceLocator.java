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

import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public interface ServiceLocator
{
    Object lookup( String role )
        throws ComponentLookupException;

    Object lookup( String role, String hint )
        throws ComponentLookupException;

    Map<String, Object> lookupMap( String role )
        throws ComponentLookupException;

    List<Object> lookupList( String role )
        throws ComponentLookupException;

    void release( Object component )
        throws ComponentLifecycleException;

    void releaseAll( Map<String, ?> components )
        throws ComponentLifecycleException;

    void releaseAll( List<?> components )
        throws ComponentLifecycleException;

    boolean hasComponent( String role );

    boolean hasComponent( String role, String hint );
}
