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
package org.codehaus.plexus.component.factory;

import org.codehaus.classworlds.ClassRealmAdapter;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

public abstract class AbstractComponentFactory
    implements ComponentFactory
{
    @SuppressWarnings( "unchecked" )
    public Object newInstance( ComponentDescriptor cd, ClassRealm realm, PlexusContainer container )
        throws ComponentInstantiationException
    {
        return newInstance( cd, ClassRealmAdapter.getInstance( realm ), container );
    }

    @SuppressWarnings( { "unchecked", "unused" } )
    protected Object newInstance( ComponentDescriptor cd, org.codehaus.classworlds.ClassRealm realm,
                                  PlexusContainer container )
        throws ComponentInstantiationException
    {
        throw new IllegalStateException( getClass() + " does not implement component creation" );
    }
}
