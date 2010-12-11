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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

final class RankedList<T>
    extends AbstractList<T>
    implements RandomAccess
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int INITIAL_CAPACITY = 10;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private int uid;

    Object[] elements = new Object[INITIAL_CAPACITY];

    long[] ranks = new long[INITIAL_CAPACITY];

    int size;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public synchronized void add( final int rank, final T element )
    {
        final long uniqueRank = uniqueRank( rank );
        final int index = ~Arrays.binarySearch( ranks, 0, size, uniqueRank );
        if ( index < 0 )
        {
            throw new IllegalStateException( "Duplicate Rank: " + uniqueRank );
        }
        if ( index >= elements.length )
        {
            final int capacity = index * 3 / 2 + 1;

            final Object[] newElements = new Object[capacity];
            System.arraycopy( elements, 0, newElements, 0, size );
            elements = newElements;

            final long[] newRanks = new long[capacity];
            System.arraycopy( ranks, 0, newRanks, 0, size );
            ranks = newRanks;
        }
        if ( index < size++ )
        {
            final int to = index + 1, len = size - to;
            System.arraycopy( elements, index, elements, to, len );
            System.arraycopy( ranks, index, ranks, to, len );
        }
        elements[index] = element;
        ranks[index] = uniqueRank;
    }

    @Override
    public synchronized boolean remove( final Object element )
    {
        for ( int index = 0; index < size; index++ )
        {
            if ( element == elements[index] )
            {
                if ( index < --size )
                {
                    final int from = index + 1, len = size - index;
                    System.arraycopy( elements, from, elements, index, len );
                    System.arraycopy( ranks, from, ranks, index, len );
                }
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public synchronized T get( final int index )
    {
        if ( index >= 0 && index < size )
        {
            return (T) elements[index];
        }
        throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private long uniqueRank( final long rank )
    {
        return -rank << 32 | 0x00000000FFFFFFFFL & uid++;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    final class Itr
        implements Iterator<T>
    {
        private long rank = Long.MIN_VALUE;

        private T nextElement;

        @SuppressWarnings( "unchecked" )
        public boolean hasNext()
        {
            if ( null == nextElement )
            {
                synchronized ( RankedList.this )
                {
                    int index = Arrays.binarySearch( ranks, 0, size, rank );
                    if ( index < 0 )
                    {
                        index = ~index;
                    }
                    nextElement = index < size ? (T) elements[index] : null;
                    rank = ++index < size ? ranks[index] : Long.MAX_VALUE;
                }
            }
            return null != nextElement;
        }

        public T next()
        {
            if ( hasNext() )
            {
                final T element = nextElement;
                nextElement = null;
                return element;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}