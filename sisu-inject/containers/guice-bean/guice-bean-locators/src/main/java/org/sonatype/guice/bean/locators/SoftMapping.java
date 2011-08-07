/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

final class SoftMapping<K, V>
    extends AbstractMap<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<K, EntryReference<K, V>> refs = new MapMaker().makeMap();

    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public V get( final Object key )
    {
        final EntryReference<K, V> ref = refs.get( key );
        return null != ref ? ref.get() : null;
    }

    @Override
    public V put( final K key, final V value )
    {
        compact();

        final EntryReference<K, V> ref = refs.put( key, new EntryReference<K, V>( key, value, queue ) );
        return null != ref ? ref.get() : null;
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        final Map<K, V> map = new HashMap<K, V>( refs.size() );
        for ( final Entry<K, EntryReference<K, V>> e : refs.entrySet() )
        {
            final V value = e.getValue().get();
            if ( null != value )
            {
                map.put( e.getKey(), value );
            }
        }
        return map.entrySet();
    }

    @Override
    public Collection<V> values()
    {
        compact();

        final List<V> list = new ArrayList<V>( refs.size() );
        for ( final EntryReference<K, V> ref : refs.values() )
        {
            final V value = ref.get();
            if ( null != value )
            {
                list.add( value );
            }
        }
        return list;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private void compact()
    {
        Reference<? extends V> ref;
        while ( ( ref = queue.poll() ) instanceof EntryReference<?, ?> )
        {
            refs.remove( ( (EntryReference<K, V>) ref ).key, ref );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class EntryReference<K, V>
        extends SoftReference<V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        K key;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        EntryReference( final K key, final V value, final ReferenceQueue<V> queue )
        {
            super( value, queue );
            this.key = key;
        }
    }
}
