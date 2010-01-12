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

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

public final class EntryListAdapter<K, V>
    extends AbstractSequentialList<V>
{
    private final Iterable<Entry<K, V>> iterable;

    public EntryListAdapter( final Iterable<Entry<K, V>> iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public ListIterator<V> listIterator( final int index )
    {
        return new ValueIterator<K, V>( iterable.iterator(), index );
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

    private static final class ValueIterator<K, V>
        implements ListIterator<V>
    {
        private final Iterator<Entry<K, V>> iterator;

        private final List<Entry<K, V>> cache = new ArrayList<Entry<K, V>>();

        private int index;

        ValueIterator( final Iterator<Entry<K, V>> iterator, final int index )
        {
            this.iterator = iterator;
            for ( int i = 0; i < index; i++ )
            {
                cache.add( iterator.next() );
            }
            this.index = index;
        }

        public boolean hasNext()
        {
            return index < cache.size() || iterator.hasNext();
        }

        public boolean hasPrevious()
        {
            return index > 0;
        }

        public V next()
        {
            if ( index >= cache.size() )
            {
                cache.add( iterator.next() );
            }
            return cache.get( index++ ).getValue();
        }

        public V previous()
        {
            if ( index <= 0 )
            {
                throw new NoSuchElementException();
            }
            return cache.get( --index ).getValue();
        }

        public int nextIndex()
        {
            return index + 1;
        }

        public int previousIndex()
        {
            return index - 1;
        }

        public void add( final V o )
        {
            throw new UnsupportedOperationException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public void set( final V o )
        {
            throw new UnsupportedOperationException();
        }
    }
}