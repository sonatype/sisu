/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.bean.locators;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * {@link Map} backed by an {@link Iterable} sequence of map entries.
 */
public final class EntryMapAdapter<K, V>
    extends AbstractMap<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Entry<K, V>> entrySet;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public EntryMapAdapter( final Iterable<? extends Entry<K, V>> iterable )
    {
        entrySet = new EntrySet<K, V>( iterable );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Entry {@link Set} backed by an {@link Iterable} sequence of map entries.
     */
    private static final class EntrySet<K, V>
        extends AbstractSet<Entry<K, V>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterable<Entry<K, V>> iterable;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        EntrySet( final Iterable iterable )
        {
            this.iterable = iterable;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Iterator<Entry<K, V>> iterator()
        {
            return iterable.iterator();
        }

        @Override
        public int size()
        {
            int size = 0;
            for ( final Iterator<?> i = iterable.iterator(); i.hasNext(); i.next() )
            {
                size++;
            }
            return size;
        }
    }
}