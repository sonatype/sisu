/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.adapters;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public final class EntryMapAdapter<K, V>
    extends AbstractMap<K, V>
{
    private final Set<Entry<K, V>> entrySet;

    public EntryMapAdapter( final Iterable<Entry<K, V>> iterable )
    {
        entrySet = new EntrySet<K, V>( iterable );
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
    }

    private static final class EntrySet<K, V>
        extends AbstractSet<Entry<K, V>>
    {
        private final Iterable<Entry<K, V>> iterable;

        EntrySet( final Iterable<Entry<K, V>> iterable )
        {
            this.iterable = iterable;
        }

        @Override
        public Iterator<Entry<K, V>> iterator()
        {
            return iterable.iterator();
        }

        @Override
        @SuppressWarnings( "unused" )
        public int size()
        {
            int size = 0;
            for ( final Entry<K, V> e : iterable )
            {
                size++;
            }
            return size;
        }
    }
}