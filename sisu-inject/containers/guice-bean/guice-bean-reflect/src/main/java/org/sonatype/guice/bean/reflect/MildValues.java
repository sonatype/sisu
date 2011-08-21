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
package org.sonatype.guice.bean.reflect;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link Map} whose values are kept alive by soft/weak {@link Reference}s; automatically compacts on read/write
 * <p>
 * Note: this class is not synchronized and all methods except {@link #get(Object)} (including iterators) may silently
 * remove elements. Concurrent access is only supported when the supplied backing map is a {@link ConcurrentMap}.
 */
final class MildValues<K, V>
    extends AbstractMap<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();

    private final ConcurrentMap<K, Reference<V>> concurrentView;

    private final Map<K, Reference<V>> map;

    private final boolean soft;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildValues( final Map<K, Reference<V>> map, final boolean soft )
    {
        this.map = map;
        this.soft = soft;

        // only do this check once: if the supplied backing map is concurrent then we are also concurrent
        concurrentView = map instanceof ConcurrentMap<?, ?> ? (ConcurrentMap<K, Reference<V>>) map : null;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public V get( final Object key )
    {
        // skip compact for performance reasons

        final Reference<V> ref = map.get( key );
        return null != ref ? ref.get() : null;
    }

    @Override
    public V put( final K key, final V value )
    {
        compact();

        final Reference<V> ref = map.put( key, mild( key, value ) );
        return null != ref ? ref.get() : null;
    }

    @Override
    public V remove( final Object key )
    {
        compact();

        final Reference<V> ref = map.remove( key );
        return null != ref ? ref.get() : null;
    }

    @Override
    public void clear()
    {
        map.clear();

        compact();
    }

    @Override
    public int size()
    {
        compact();

        return map.size();
    }

    @Override
    public Set<K> keySet()
    {
        compact();

        return map.keySet();
    }

    @Override
    public Collection<V> values()
    {
        compact();

        // avoid passing back null/cleared values
        final List<V> list = new ArrayList<V>();
        for ( final Reference<V> ref : map.values() )
        {
            final V value = ref.get();
            if ( null != value )
            {
                list.add( value );
            }
        }
        return list;
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        compact();

        // avoid passing back null/cleared entries
        final Map<K, V> entries = new HashMap<K, V>();
        for ( final Entry<K, Reference<V>> entry : map.entrySet() )
        {
            final V value = entry.getValue().get();
            if ( null != value )
            {
                entries.put( entry.getKey(), value );
            }
        }
        return entries.entrySet();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Soft or weak {@link Reference} item for the given entry.
     */
    private Reference<V> mild( final K key, final V value )
    {
        return soft ? new Soft<K, V>( key, value, queue ) : new Weak<K, V>( key, value, queue );
    }

    /**
     * Compacts the map by removing cleared items.
     */
    private void compact()
    {
        Reference<? extends V> ref;
        while ( ( ref = queue.poll() ) != null )
        {
            // only remove value if it still matches
            final Object key = ( (Item) ref ).key();
            if ( null != concurrentView )
            {
                concurrentView.remove( key, ref );
            }
            else if ( map.get( key ) == ref )
            {
                map.remove( key );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Common functionality shared by both soft and weak items in the map.
     */
    private static interface Item
    {
        /**
         * @return The owning key
         */
        Object key();
    }

    /**
     * Soft {@link Item} that keeps track of its key so it can be removed when the value is unreachable.
     */
    private static final class Soft<K, V>
        extends SoftReference<V>
        implements Item
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final K key;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Soft( final K key, final V value, final ReferenceQueue<V> queue )
        {
            super( value, queue );
            this.key = key;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Object key()
        {
            return key;
        }
    }

    /**
     * Weak {@link Item} that keeps track of its key so it can be removed when the value is unreachable.
     */
    private static final class Weak<K, V>
        extends WeakReference<V>
        implements Item
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final K key;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Weak( final K key, final V value, final ReferenceQueue<V> queue )
        {
            super( value, queue );
            this.key = key;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Object key()
        {
            return key;
        }
    }
}
