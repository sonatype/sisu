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

    Object[] elements;

    long[] uids;

    volatile int size;

    volatile int insertCount;

    boolean isCached;

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
            uids = new long[INITIAL_CAPACITY];
        }
        else if ( size >= elements.length )
        {
            final int capacity = size * 3 / 2 + 1;

            final Object[] newElements = new Object[capacity];
            System.arraycopy( elements, 0, newElements, 0, size );
            elements = newElements;

            final long[] newUIDs = new long[capacity];
            System.arraycopy( uids, 0, newUIDs, 0, size );
            uids = newUIDs;
        }
        else if ( isCached )
        {
            isCached = false;
            elements = elements.clone();
            uids = uids.clone();
        }
        final long uid = rank2uid( rank, insertCount++ );
        final int index = safeBinarySearch( uid );
        if ( index < size++ )
        {
            final int to = index + 1, len = size - to;
            System.arraycopy( elements, index, elements, to, len );
            System.arraycopy( uids, index, uids, to, len );
        }
        elements[index] = element;
        uids[index] = uid;
    }

    @Override
    public synchronized T remove( final int index )
    {
        final T element = get( index );
        if ( isCached )
        {
            isCached = false;
            elements = elements.clone();
            uids = uids.clone();
        }
        if ( index < --size )
        {
            final int from = index + 1, len = size - index;
            System.arraycopy( elements, from, elements, index, len );
            System.arraycopy( uids, from, uids, index, len );
        }
        elements[size] = null;
        return element;
    }

    public synchronized void update( final T element, final int rank )
    {
        for ( int i = 0; i < size; i++ )
        {
            if ( element.equals( elements[i] ) )
            {
                remove( i );
                break;
            }
        }
        insert( element, rank );
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
     * Returns the rank assigned to the element at the given index.
     * 
     * @param index The element index
     * @return Rank assigned to the element
     */
    public synchronized int getRank( final int index )
    {
        if ( index < 0 || index >= size )
        {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
        }
        return uid2rank( uids[index] );
    }

    @Override
    public synchronized boolean remove( final Object element )
    {
        for ( int i = 0; i < size; i++ )
        {
            if ( element.equals( elements[i] ) )
            {
                return null != remove( i );
            }
        }
        return false;
    }

    /**
     * Removes the given element from the list; uses <b>identity</b> comparison instead of equality.
     * 
     * @param element The element to remove
     * @return {@code true} if the element was removed; otherwise {@code false}
     */
    public synchronized boolean removeSame( final Object element )
    {
        for ( int i = 0; i < size; i++ )
        {
            if ( element == elements[i] )
            {
                return null != remove( i );
            }
        }
        return false;
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
            if ( null != elements )
            {
                clone.elements = elements.clone();
                clone.uids = uids.clone();
            }
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
        elements = null;
        uids = null;

        size = 0;
        insertCount = 0;
        isCached = false;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public Itr iterator()
    {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Turns the given (potentially non-unique) rank into a unique id by appending a counter.
     * 
     * @param rank The assigned rank
     * @param uniq The unique counter
     * @return The unique id
     */
    private static long rank2uid( final int rank, final int uniq )
    {
        return (long) ~rank << 32 | 0x00000000FFFFFFFFL & uniq;
    }

    /**
     * Extracts the original (potentially non-unique) assigned rank from the given unique id.
     * 
     * @param uid The unique id
     * @return Assigned rank
     */
    static int uid2rank( final long uid )
    {
        return (int) ( ~uid >>> 32 );
    }

    /**
     * Finds the insertion point with the nearest UID, regardless of whether the UID is in the list or not.<br>
     * Unlike {@link Arrays#binarySearch} this will always return a number from zero to {@link #size()} inclusive.
     * 
     * @param uid The UID to find
     * @return Index with nearest UID
     */
    int safeBinarySearch( final long uid )
    {
        int min = 0;
        int max = size - 1;
        final int end = max;
        while ( min < max )
        {
            final int m = min + max >>> 1;
            if ( uid <= uids[m] )
            {
                max = m;
            }
            else
            {
                min = m + 1;
            }
        }
        if ( end == min && uids[min] < uid )
        {
            return min + 1; // append
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

        private Object[] cachedElements;

        private long[] cachedUIDs;

        private long lastKnownState;

        private long nextUID = Long.MIN_VALUE;

        private T nextElement;

        private int index;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        public boolean hasNext()
        {
            if ( null != nextElement )
            {
                return true;
            }
            if ( safeHasNext() )
            {
                nextElement = (T) cachedElements[index];
                nextUID = cachedUIDs[index] + 1;
                return true;
            }
            return false;
        }

        /**
         * @return Rank assigned to the next element; returns {@link Integer#MIN_VALUE} if there is no next element.
         */
        public int peekNextRank()
        {
            if ( null != nextElement )
            {
                return uid2rank( nextUID );
            }
            if ( safeHasNext() )
            {
                return uid2rank( cachedUIDs[index] );
            }
            return Integer.MIN_VALUE;
        }

        public T next()
        {
            if ( hasNext() )
            {
                index++;

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

        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        /**
         * Finds out if there is a next element, regardless of any intervening updates to the list.
         * 
         * @return {@code true} if there is a next element; otherwise {@code false}
         */
        private boolean safeHasNext()
        {
            if ( lastKnownState != ( (long) insertCount << 32 | size ) )
            {
                synchronized ( RankedList.this )
                {
                    // reposition ourselves in the list
                    index = safeBinarySearch( nextUID );
                    if ( index < size )
                    {
                        isCached = true;
                        cachedElements = elements;
                        cachedUIDs = uids;
                    }
                    lastKnownState = ( (long) insertCount << 32 | size );
                }
            }
            return index < (int) lastKnownState;
        }
    }
}