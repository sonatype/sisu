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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Sorted {@link List} which arranges elements by descending rank; allows concurrent iteration and modification.
 */
final class RankedList<T>
    extends AbstractList<T>
    implements RandomAccess, Cloneable
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int INITIAL_CAPACITY = 10;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private int uid;

    Object[] elements;

    long[] ranks;

    int size;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Inserts the given element into the sorted list, using the assigned rank as a guide.<br>
     * The rank can be any value from {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}.
     * 
     * @param element The element to insert
     * @param rank The assigned rank
     */
    public synchronized void insert( final T element, final int rank )
    {
        if ( null == elements )
        {
            elements = new Object[INITIAL_CAPACITY];
            ranks = new long[INITIAL_CAPACITY];
        }
        else if ( size >= elements.length )
        {
            final int capacity = size * 3 / 2 + 1;

            final Object[] newElements = new Object[capacity];
            System.arraycopy( elements, 0, newElements, 0, size );
            elements = newElements;

            final long[] newRanks = new long[capacity];
            System.arraycopy( ranks, 0, newRanks, 0, size );
            ranks = newRanks;
        }
        final long uniqueRank = uniqueRank( rank );
        final int index = safeBinarySearch( uniqueRank );
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
    @SuppressWarnings( "unchecked" )
    public synchronized T get( final int index )
    {
        if ( index < 0 || index >= size )
        {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
        }
        return (T) elements[index];
    }

    /**
     * @return The highest rank; this is the rank assigned to the first element in the list.
     */
    public synchronized int maxRank()
    {
        return size > 0 ? (int) ( ~ranks[0] >>> 32 ) : Integer.MIN_VALUE;
    }

    /**
     * @return The lowest rank; this is the rank assigned to the last element in the list.
     */
    public synchronized int minRank()
    {
        return size > 0 ? (int) ( ~ranks[size - 1] >>> 32 ) : Integer.MIN_VALUE;
    }

    @Override
    public synchronized T remove( final int index )
    {
        final T element = get( index );
        if ( index < --size )
        {
            final int from = index + 1, len = size - index;
            System.arraycopy( elements, from, elements, index, len );
            System.arraycopy( ranks, from, ranks, index, len );
        }
        return element;
    }

    /**
     * @return Shallow copy of this {@link RankedList} instance.
     */
    @Override
    public synchronized RankedList<T> clone()
    {
        try
        {
            @SuppressWarnings( "unchecked" )
            final RankedList<T> clone = (RankedList<T>) super.clone();
            clone.elements = null != elements ? elements.clone() : null;
            clone.ranks = null != ranks ? ranks.clone() : null;
            return clone;
        }
        catch ( final CloneNotSupportedException e )
        {
            throw new InternalError();
        }
    }

    @Override
    public synchronized void clear()
    {
        uid = 0;
        elements = null;
        ranks = null;
        size = 0;
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

    /**
     * Attempts to turn the given non-unique rank into a unique rank by appending a counter.
     * 
     * @param rank The non-unique rank
     * @return The unique rank
     */
    private long uniqueRank( final long rank )
    {
        return ~rank << 32 | 0x00000000FFFFFFFFL & uid++;
    }

    /**
     * Finds the insertion point nearest to the given rank, regardless of whether the rank is in the list or not.<br>
     * Unlike {@link Arrays#binarySearch} this always returns a natural number from zero to {@link #size()} inclusive.
     * 
     * @param rank The rank to find
     * @return Index nearest to rank
     */
    int safeBinarySearch( final long rank )
    {
        if ( size == 0 || rank <= ranks[0] )
        {
            return 0;
        }

        int max = size - 1;
        int min = rank < ranks[max] ? 0 : max;
        while ( min <= max )
        {
            final int m = min + max >>> 1;
            final long midRank = ranks[m];
            if ( rank < midRank )
            {
                max = m - 1;
            }
            else if ( midRank < rank )
            {
                min = m + 1;
            }
            else
            {
                return m;
            }
        }
        return min;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Custom {@link Iterator} that copes with modification by repositioning itself in the updated list.
     */
    final class Itr
        implements Iterator<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private long nextRank = Long.MIN_VALUE;

        private T nextElement;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        public boolean hasNext()
        {
            if ( null == nextElement && nextRank < Long.MAX_VALUE )
            {
                synchronized ( RankedList.this )
                {
                    final int index = safeBinarySearch( nextRank );
                    if ( index < size )
                    {
                        nextElement = (T) elements[index];
                        nextRank = ranks[index] + 1;
                    }
                }
            }
            return null != nextElement;
        }

        public T next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
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