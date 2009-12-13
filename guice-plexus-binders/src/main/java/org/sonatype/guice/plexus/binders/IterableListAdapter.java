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
package org.sonatype.guice.plexus.binders;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

final class IterableListAdapter<T>
    extends AbstractSequentialList<T>
{
    private final Iterable<Entry<String, T>> iterable;

    IterableListAdapter( final Iterable<Entry<String, T>> iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public ListIterator<T> listIterator( final int index )
    {
        final List<Entry<String, T>> cache = new ArrayList<Entry<String, T>>();
        final Iterator<Entry<String, T>> iterator = iterable.iterator();

        for ( int i = 0; i < index; i++ )
        {
            cache.add( iterator.next() );
        }

        return new ListIterator<T>()
        {
            private int cursor = index;

            public boolean hasNext()
            {
                return cursor < cache.size() || iterator.hasNext();
            }

            public boolean hasPrevious()
            {
                return cursor > 0;
            }

            public T next()
            {
                if ( cursor >= cache.size() )
                {
                    cache.add( iterator.next() );
                }
                return cache.get( cursor++ ).getValue();
            }

            public T previous()
            {
                if ( cursor <= 0 )
                {
                    throw new NoSuchElementException();
                }
                return cache.get( --cursor ).getValue();
            }

            public int nextIndex()
            {
                return cursor + 1;
            }

            public int previousIndex()
            {
                return cursor - 1;
            }

            public void add( final T o )
            {
                throw new UnsupportedOperationException();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }

            public void set( final T o )
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int size()
    {
        final Iterator<Entry<String, T>> i = iterable.iterator();

        int size = 0;
        while ( i.hasNext() )
        {
            i.next();
            size++;
        }

        return size;
    }
}