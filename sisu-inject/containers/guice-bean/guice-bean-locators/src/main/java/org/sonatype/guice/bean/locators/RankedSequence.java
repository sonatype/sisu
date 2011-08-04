/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sorted {@link List} that arranges elements by descending rank; supports concurrent iteration and modification.
 */
final class RankedSequence<T>
    implements Iterable<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int INITIAL_CAPACITY = 10;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final AtomicReference<Contents> cache = new AtomicReference<Contents>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RankedSequence()
    {
    }

    RankedSequence( final RankedSequence<T> sequence )
    {
        final Contents contents = sequence.cache.get();
        if ( null != contents )
        {
            cache.set( new Contents( contents ) );
        }
    }

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
        Contents oldContents, newContents;

        do
        {
            if ( null == ( oldContents = cache.get() ) )
            {
                newContents = new Contents( element, rank );
                continue;
            }
            synchronized ( oldContents )
            {
                newContents = oldContents.isImmutable ? new Contents( oldContents ) : oldContents;

                if ( newContents.size >= newContents.objs.length )
                {
                    final int capacity = newContents.size * 3 / 2 + 1;

                    final Object[] newObjs = new Object[capacity];
                    System.arraycopy( newContents.objs, 0, newObjs, 0, newContents.size );

                    final long[] newUIDs = new long[capacity];
                    System.arraycopy( newContents.uids, 0, newUIDs, 0, newContents.size );

                    newContents.objs = newObjs;
                    newContents.uids = newUIDs;
                }

                final long uid = rank2uid( rank, newContents.uniq++ );
                final int index = safeBinarySearch( newContents, uid );

                final int to = index + 1, len = newContents.size - index;
                if ( len > 0 )
                {
                    System.arraycopy( newContents.objs, index, newContents.objs, to, len );
                    System.arraycopy( newContents.uids, index, newContents.uids, to, len );
                }
                newContents.size++;

                newContents.objs[index] = element;
                newContents.uids[index] = uid;
            }
        }
        while ( oldContents != newContents && !cache.compareAndSet( oldContents, newContents ) );
    }

    @SuppressWarnings( "unchecked" )
    public T remove( final int index )
    {
        T element;

        Contents oldContents, newContents;

        do
        {
            if ( null == ( oldContents = cache.get() ) )
            {
                throw new IndexOutOfBoundsException( "Index: " + index + ", Size: 0" );
            }
            synchronized ( oldContents )
            {
                if ( index >= oldContents.size )
                {
                    throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + oldContents.size );
                }
                newContents = oldContents.isImmutable ? new Contents( oldContents ) : oldContents;

                element = (T) newContents.objs[index];

                final int from = index + 1, len = newContents.size - from;
                if ( len > 0 )
                {
                    System.arraycopy( newContents.objs, from, newContents.objs, index, len );
                    System.arraycopy( newContents.uids, from, newContents.uids, index, len );
                }
                newContents.objs[--newContents.size] = null; // remove dangling reference
            }
        }
        while ( oldContents != newContents && !cache.compareAndSet( oldContents, newContents ) );

        return element;
    }

    public int indexOf( final Object o )
    {
        final Contents contents = cache.get();
        for ( int i = 0, size = null != contents ? contents.size : 0; i < size; i++ )
        {
            if ( o.equals( contents.objs[i] ) )
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Similar function to {@link #indexOf(Object)} except it uses {@code ==} instead of {@code equals}.
     * 
     * @see #indexOf(Object)
     */
    public int indexOfThis( final T element )
    {
        final Contents contents = cache.get();
        for ( int i = 0, size = null != contents ? contents.size : 0; i < size; i++ )
        {
            if ( element == contents.objs[i] )
            {
                return i;
            }
        }
        return -1;
    }

    public boolean contains( final Object o )
    {
        return indexOf( o ) >= 0;
    }

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
        final Contents contents = cache.get();
        if ( null == contents )
        {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: 0" );
        }
        if ( index < contents.size )
        {
            return (T) contents.objs[index];
        }
        throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + contents.size );
    }

    /**
     * Returns the rank assigned to the element at the given index.
     * 
     * @param index The element index
     * @return Rank assigned to the element
     */
    public int getRank( final int index )
    {
        final Contents contents = cache.get();
        if ( null == contents )
        {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: 0" );
        }
        if ( index < contents.size )
        {
            return uid2rank( contents.uids[index] );
        }
        throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + contents.size );
    }

    public void clear()
    {
        cache.set( null );
    }

    public int size()
    {
        final Contents contents = cache.get();
        return null == contents ? 0 : contents.size;
    }

    public boolean isEmpty()
    {
        return 0 == size();
    }

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
    static long rank2uid( final int rank, final int uniq )
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
     * @param contents The contents
     * @param uid The UID to find
     * @return Index with nearest UID
     */
    static int safeBinarySearch( final Contents contents, final long uid )
    {
        int min = 0;
        int max = contents.size - 1;
        while ( min < max )
        {
            final int m = min + max >>> 1;
            if ( uid <= contents.uids[m] )
            {
                max = m;
            }
            else
            {
                min = m + 1;
            }
        }
        if ( min == contents.size - 1 && contents.uids[min] < uid )
        {
            return contents.size; // append
        }
        return min;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class Contents
    {
        volatile boolean isImmutable;

        Object[] objs;

        long[] uids;

        int size;

        int uniq;

        Contents( final Object element, final int rank )
        {
            objs = new Object[INITIAL_CAPACITY];
            uids = new long[INITIAL_CAPACITY];

            objs[0] = element;
            uids[0] = rank2uid( rank, uniq++ );

            size++;
        }

        Contents( final Contents contents )
        {
            size = contents.size;
            uniq = contents.uniq;

            if ( size >= contents.objs.length )
            {
                final int capacity = size * 3 / 2 + 1;

                final Object[] newObjs = new Object[capacity];
                System.arraycopy( contents.objs, 0, newObjs, 0, size );

                final long[] newUIDs = new long[capacity];
                System.arraycopy( contents.uids, 0, newUIDs, 0, size );

                objs = newObjs;
                uids = newUIDs;
            }
            else
            {
                objs = contents.objs.clone();
                uids = contents.uids.clone();
            }
        }
    }

    /**
     * Custom {@link Iterator} that copes with modification by repositioning itself in the updated list.
     */
    final class Itr
        implements Iterator<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Contents contents;

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
                nextObj = (T) contents.objs[index];
                nextUID = contents.uids[index];
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
                return uid2rank( contents.uids[index] );
            }
            return Integer.MIN_VALUE;
        }

        public T next()
        {
            if ( hasNext() )
            {
                nextUID++; // guarantees progress when re-positioning
                index++;

                // populated by hasNext()
                final T element = nextObj;
                nextObj = null;
                return element;
            }
            throw new NoSuchElementException();
        }

        public int rank()
        {
            return uid2rank( nextUID );
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
            final Contents newContents = cache.get();
            if ( contents != newContents )
            {
                if ( null == ( contents = newContents ) )
                {
                    return false;
                }
                if ( contents.isImmutable )
                {
                    index = safeBinarySearch( contents, nextUID );
                }
                else
                {
                    synchronized ( contents )
                    {
                        contents.isImmutable = true;
                        index = safeBinarySearch( contents, nextUID );
                    }
                }
            }
            return null != contents && index < contents.size;
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
