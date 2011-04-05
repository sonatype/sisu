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
import java.util.List;
import java.util.Map;
import java.util.Set;

final class MergedParameters
    extends AbstractMap<String, String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, String>[] parameters;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    MergedParameters( final List<Map<String, String>> parameters )
    {
        this.parameters = parameters.toArray( new Map[parameters.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public String get( final Object key )
    {
        for ( final Map<String, String> p : parameters )
        {
            final Object value = p.get( key );
            if ( null != value )
            {
                return String.valueOf( value );
            }
        }
        return null;
    }

    @Override
    public boolean containsKey( final Object key )
    {
        for ( final Map<String, String> p : parameters )
        {
            if ( p.containsKey( key ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public Set<Entry<String, String>> entrySet()
    {
        final Set entries = new HashSet();
        for ( final Map<String, String> p : parameters )
        {
            entries.addAll( p.entrySet() );
        }
        return entries;
    }
}
