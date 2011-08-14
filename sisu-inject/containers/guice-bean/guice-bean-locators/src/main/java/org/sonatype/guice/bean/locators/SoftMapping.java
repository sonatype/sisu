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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class SoftMapping<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<K, EntryReference<K, V>> referenceMap;

    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    SoftMapping( final int initialCapacity, final float loadFactor, final int concurrencyLevel )
    {
        referenceMap = new ConcurrentHashMap<K, EntryReference<K, V>>( initialCapacity, loadFactor, concurrencyLevel );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public V get( final Object key )
    {
        final EntryReference<K, V> ref = referenceMap.get( key );
        return null != ref ? ref.get() : null;
    }

    public V put( final K key, final V value )
    {
        compact();

        final EntryReference<K, V> ref = referenceMap.put( key, new EntryReference<K, V>( key, value, queue ) );
        return null != ref ? ref.get() : null;
    }

    public Collection<V> values()
    {
        compact();

        final List<V> list = new ArrayList<V>();
        for ( final EntryReference<K, V> ref : referenceMap.values() )
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
            referenceMap.remove( ( (EntryReference<K, V>) ref ).key, ref );
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
