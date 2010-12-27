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

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Sorted {@link List} that arranges elements by descending rank; supports concurrent iteration and modification.
 */
final class RankedList<T>
    extends AbstractCollection<T>
    implements List<T>, RandomAccess, Cloneable
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int INITIAL_CAPACITY = 10;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    Object[] objs;

    long[] uids;

    volatile int size;

    volatile int uniq;

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
    public void insert( final T element, final int rank )
    {
        if ( null == objs )
        {
            objs = new Object[INITIAL_CAPACITY];
            uids = new long[INITIAL_CAPACITY];
        }
        else if ( size >= objs.length )
        {
            final int capacity = size * 3 / 2 + 1;

            final Object[] newObjs = new Object[capacity];
            System.arraycopy( objs, 0, newObjs, 0, size );

            final long[] newUIDs = new long[capacity];
            System.arraycopy( uids, 0, newUIDs, 0, size );

            objs = newObjs;
            uids = newUIDs;
            isCached = false;
        }
        else if ( isCached )
        {
            // copy-on-write
            objs = objs.clone();
            uids = uids.clone();
            isCached = false;
        }

        final long uid = rank2uid( rank, uniq++ );
        final int index = safeBinarySearch( uid );

        // make space: use loop as the size will be small
        for ( int j = size, i = j--; i > index; i = j-- )
        {
            objs[i] = objs[j];
            uids[i] = uids[j];
        }
        size++;

        objs[index] = element;
        uids[index] = uid;
    }

    public T remove( final int index )
    {
        final T element = get( index );

        if ( isCached )
        {
            // copy-on-write
            objs = objs.clone();
            uids = uids.clone();
            isCached = false;
        }

        // compact space: use loop as the size will be small
        for ( int j = index, i = j++; j < size; i = j++ )
        {
            objs[i] = objs[j];
            uids[i] = uids[j];
        }
        size--;

        objs[size] = null; // remove dangling reference

        return element;
    }

    public int indexOf( final Object o )
    {
        for ( int i = 0; i < size; i++ )
        {
            if ( o.equals( objs[i] ) )
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Similar function to {@link #indexOf(Object)} except it uses {@code ==} instead of {@code equals}.
     * 
     * @param o The exact element to search for
     * @see #indexOf(Object)
     */
    public int indexOfThis( final T element )
    {
        for ( int i = 0; i < size; i++ )
        {
            if ( element == objs[i] )
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean contains( final Object o )
    {
        return indexOf( o ) >= 0;
    }

    @Override
    public boolean remove( final Object o )
    {
        final int index = indexOf( o );
        if ( index >= 0 )
        {
            remove( index );
            return true;
        }
        return false;
    }

    @SuppressWarnings( "unchecked" )
    public T get( final int index )
    {
        if ( index < size )
        {
            return (T) objs[index];
        }
        throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
    }

    /**
     * Returns the rank assigned to the element at the given index.
     * 
     * @param index The element index
     * @return Rank assigned to the element
     */
    public int getRank( final int index )
    {
        if ( index < size )
        {
            return uid2rank( uids[index] );
        }
        throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
    }

    /**
     * @return Shallow copy of this {@link RankedList} instance.
     */
    @Override
    public RankedList<T> clone()
    {
        try
        {
            @SuppressWarnings( "unchecked" )
            final RankedList<T> clone = (RankedList<T>) super.clone();
            if ( null != objs )
            {
                clone.objs = objs.clone();
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
    public void clear()
    {
        objs = null;
        uids = null;

        size = 0;
        uniq = 0;

        isCached = false;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean isEmpty()
    {
        return 0 == size;
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
        if ( min == size - 1 && uids[min] < uid )
        {
            return size; // append
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

        private Object[] cachedObjs;

        private long[] cachedUIDs;

        private int expectedSize;

        private int expectedUniq;

        private long nextUID = Long.MIN_VALUE;

        private T nextObj;

        private int index;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        public boolean hasNext()
        {
            if ( null != nextObj )
            {
                return true;
            }
            if ( safeHasNext() )
            {
                nextObj = (T) cachedObjs[index];
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
            if ( null != nextObj )
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
                final T element = nextObj;
                nextObj = null;
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
            if ( expectedSize != size || expectedUniq != uniq )
            {
                synchronized ( RankedList.this )
                {
                    // reposition ourselves in the list
                    index = safeBinarySearch( nextUID );

                    if ( index < size )
                    {
                        cachedObjs = objs;
                        cachedUIDs = uids;
                        isCached = true;
                    }

                    expectedSize = size;
                    expectedUniq = uniq;
                }
            }
            return index < expectedSize;
        }
    }

    // ----------------------------------------------------------------------
    // Unsupported methods
    // ----------------------------------------------------------------------

    @Deprecated
    public void add( final int index, final T element )
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public boolean addAll( final int index, final Collection<? extends T> c )
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public T set( final int index, final T element )
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public int lastIndexOf( final Object o )
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public ListIterator<T> listIterator()
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public ListIterator<T> listIterator( final int index )
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public List<T> subList( final int fromIndex, final int toIndex )
    {
        throw new UnsupportedOperationException();
    }
}