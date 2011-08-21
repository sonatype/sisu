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
package org.sonatype.guice.bean.reflect;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link Map} whose keys are kept alive by soft/weak {@link Reference}s; automatically compacts on read/write
 * <p>
 * Note: this class is not synchronized and all methods except {@link #get(Object)} (including iterators) may silently
 * remove elements. Concurrent access is only supported when the supplied backing map is a {@link ConcurrentMap}.
 */
final class MildKeys<K, V>
    extends AbstractMap<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ReferenceQueue<K> queue = new ReferenceQueue<K>();

    private final Map<Reference<K>, V> map;

    private final boolean soft;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildKeys( final Map<Reference<K>, V> map, final boolean soft )
    {
        this.map = map;
        this.soft = soft;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public V get( final Object key )
    {
        // skip compact for performance reasons

        return map.get( mild( key ) );
    }

    @Override
    public V put( final K key, final V value )
    {
        compact();

        return map.put( mild( key ), value );
    }

    @Override
    public V remove( final Object key )
    {
        compact();

        return map.remove( mild( key ) );
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

        // avoid passing back null/cleared keys
        final Set<K> set = new HashSet<K>();
        for ( final Reference<K> ref : map.keySet() )
        {
            final K key = ref.get();
            if ( null != key )
            {
                set.add( key );
            }
        }
        return set;
    }

    @Override
    public Collection<V> values()
    {
        compact();

        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        compact();

        // avoid passing back null/cleared entries
        final Map<K, V> entries = new HashMap<K, V>();
        for ( final Entry<Reference<K>, V> entry : map.entrySet() )
        {
            final K key = entry.getKey().get();
            if ( null != key )
            {
                entries.put( key, entry.getValue() );
            }
        }
        return entries.entrySet();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Soft or weak {@link Reference} item for the given key.
     */
    @SuppressWarnings( "unchecked" )
    private Reference<K> mild( final Object key )
    {
        return soft ? new Soft<K>( (K) key, queue ) : new Weak<K>( (K) key, queue );
    }

    /**
     * Compacts the map by removing cleared keys.
     */
    private void compact()
    {
        Reference<? extends K> ref;
        while ( ( ref = queue.poll() ) != null )
        {
            map.remove( ref );
        }
    }

    /**
     * Compares a sample {@link Reference} to a given object using referential equality.
     * 
     * @param lhs The sample reference
     * @param rhs The object to compare
     * @return {@code true} if same reference or same referent; otherwise {@code false}
     */
    static <K> boolean same( final Reference<K> lhs, final Object rhs )
    {
        if ( lhs == rhs )
        {
            return true; // exact same reference
        }
        final K key = lhs.get();
        if ( null != key && rhs instanceof Reference<?> )
        {
            // different reference, but same referent
            return key == ( (Reference<?>) rhs ).get();
        }
        return false;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Soft key that maintains a constant hash and uses referential equality.
     */
    private static final class Soft<K>
        extends SoftReference<K>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final int hash;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Soft( final K key, final ReferenceQueue<K> queue )
        {
            super( key, queue );
            hash = key.hashCode();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public int hashCode()
        {
            return hash;
        }

        @Override
        public boolean equals( final Object rhs )
        {
            return MildKeys.same( this, rhs );
        }
    }

    /**
     * Weak key that maintains a constant hash and uses referential equality.
     */
    private static final class Weak<K>
        extends WeakReference<K>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final int hash;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Weak( final K key, final ReferenceQueue<K> queue )
        {
            super( key, queue );
            hash = key.hashCode();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public int hashCode()
        {
            return hash;
        }

        @Override
        public boolean equals( final Object rhs )
        {
            return MildKeys.same( this, rhs );
        }
    }
}
