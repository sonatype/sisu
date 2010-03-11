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

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

/**
 * {@link List} backed by an {@link Iterable} sequence of map entries.
 */
public final class EntryListAdapter<K, V>
    extends AbstractSequentialList<V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Entry<K, V>> iterable;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public EntryListAdapter( final Iterable<Entry<K, V>> iterable )
    {
        this.iterable = iterable;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public ListIterator<V> listIterator( final int index )
    {
        return new ValueIterator<K, V>( iterable, index );
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

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link ListIterator} backed by a cache of map entries, fed from an {@link Iterator}.
     */
    private static final class ValueIterator<K, V>
        implements ListIterator<V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Entry<K, V>> iterator;

        private final List<Entry<K, V>> entryCache = new ArrayList<Entry<K, V>>();

        private int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ValueIterator( final Iterable<Entry<K, V>> iterable, final int index )
        {
            if ( index < 0 )
            {
                throw new IndexOutOfBoundsException();
            }
            this.iterator = iterable.iterator();
            try
            {
                while ( this.index < index )
                {
                    next(); // position iterator at the index position
                }
            }
            catch ( final NoSuchElementException e )
            {
                throw new IndexOutOfBoundsException();
            }
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return index < entryCache.size() || iterator.hasNext();
        }

        public boolean hasPrevious()
        {
            return index > 0;
        }

        public V next()
        {
            if ( index >= entryCache.size() )
            {
                entryCache.add( iterator.next() );
            }
            return entryCache.get( index++ ).getValue();
        }

        public V previous()
        {
            if ( index <= 0 )
            {
                throw new NoSuchElementException();
            }
            return entryCache.get( --index ).getValue();
        }

        public int nextIndex()
        {
            return index;
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