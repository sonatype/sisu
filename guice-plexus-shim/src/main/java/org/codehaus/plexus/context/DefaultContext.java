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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultContext
    implements Context
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Object, Object> contextData = new ConcurrentHashMap<Object, Object>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultContext()
    {
        // nothing to store
    }

    public DefaultContext( final Map<?, ?> context )
    {
        if ( context != null )
        {
            for ( final Entry<?, ?> e : context.entrySet() )
            {
                put( e.getKey(), e.getValue() );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean contains( final Object key )
    {
        return contextData.containsKey( key );
    }

    public void put( final Object key, final Object value )
    {
        if ( null == key )
        {
            throw new IllegalArgumentException( "Key is null" );
        }
        if ( null != value )
        {
            contextData.put( key, value );
        }
        else
        {
            contextData.remove( key );
        }
    }

    public Object get( final Object key )
        throws ContextException
    {
        final Object data = contextData.get( key );
        if ( data == null )
        {
            throw new ContextException( "Unable to resolve context key: " + key );
        }
        return data;
    }

    public Map<Object, Object> getContextData()
    {
        return Collections.unmodifiableMap( contextData );
    }

    @Override
    public String toString()
    {
        return contextData.toString();
    }
}
