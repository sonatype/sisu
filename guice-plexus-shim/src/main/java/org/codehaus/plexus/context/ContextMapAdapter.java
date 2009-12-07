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
import java.util.Set;

public final class ContextMapAdapter
    extends AbstractMap<Object, Object>
{
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Context context;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ContextMapAdapter( final Context context )
    {
        this.context = context;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Object get( final Object key )
    {
        try
        {
            final Object value = context.get( key );
            return value instanceof String ? value : null;
        }
        catch ( final ContextException e )
        {
            return null;
        }
    }

    @Override
    public Set<Entry<Object, Object>> entrySet()
    {
        final Set<Entry<Object, Object>> tempSet = new HashSet<Entry<Object, Object>>();
        for ( final Entry<Object, Object> entry : context.getContextData().entrySet() )
        {
            if ( entry.getValue() instanceof String )
            {
                tempSet.add( entry );
            }
        }
        return tempSet;
    }
}
