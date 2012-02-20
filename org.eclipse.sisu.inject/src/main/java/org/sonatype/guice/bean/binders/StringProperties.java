/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.binders;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class StringProperties
    extends AbstractMap<String, String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    StringProperties( final Map<?, ?> delegate )
    {
        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public String get( final Object key )
    {
        final Object value = delegate.get( key );
        if ( value instanceof String )
        {
            return (String) value;
        }
        return null;
    }

    @Override
    public boolean containsKey( final Object key )
    {
        final Object value = delegate.get( key );
        if ( null == value )
        {
            return delegate.containsKey( key );
        }
        return value instanceof String;
    }

    @Override
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public Set<Entry<String, String>> entrySet()
    {
        final Set entries = new HashSet();
        for ( final Entry e : delegate.entrySet() )
        {
            if ( e.getKey() instanceof String )
            {
                final Object value = e.getValue();
                if ( null == value || value instanceof String )
                {
                    entries.add( e );
                }
            }
        }
        return entries;
    }
}
