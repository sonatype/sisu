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
package org.codehaus.plexus.context;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ContextMapAdapter
    extends AbstractMap<Object, Object>
{
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Map<Object, Object> contextData;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ContextMapAdapter( final Context context )
    {
        contextData = context.getContextData();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public boolean containsKey( final Object key )
    {
        return get( key ) != null;
    }

    @Override
    public Object get( final Object key )
    {
        final Object value = contextData.get( key );
        return value instanceof String ? value : null;
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    @Override
    public Set<Entry<Object, Object>> entrySet()
    {
        final Set<Entry<Object, Object>> tempSet = new HashSet<Entry<Object, Object>>();
        for ( final Entry<Object, Object> entry : contextData.entrySet() )
        {
            if ( entry.getValue() instanceof String )
            {
                tempSet.add( entry );
            }
        }
        return tempSet;
    }
}
