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

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ordered {@link List} that arranges elements by descending rank; supports concurrent iteration and modification.
 */
final class RankedSequence<T>
    extends AbstractCollection<T>
    implements Collection<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int INITIAL_CAPACITY = 10;

    private static final Object[] NO_ELEMENTS = {};

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
        final Contents contents = null != sequence ? sequence.cache.get() : null;
        if ( null != contents )
        {
            cache.set( new Contents( contents ) );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Inserts the given element into the ordered list, using the assigned rank as a guide.<br>
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
            }
            else
            {
                synchronized ( oldContents )
                {
                    newContents = insert( oldContents, element, rank );
                }
            }
        }
        while ( oldContents != newContents && !cache.compareAndSet( oldContents, newContents ) );
    }

    public boolean add( final T element )
    {
        insert( element, 0 );
        return true;
    }

    public boolean contains( final Object element )
    {
        final Contents contents = cache.get();
        if ( null != contents )
        {
            for ( final Object o : contents.objs )
            {
                if ( element.equals( o ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsThis( final T element )
    {
        final Contents contents = cache.get();
        if ( null != contents )
        {
            for ( final Object o : contents.objs )
            {
                if ( element == o )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean remove( final Object element )
    {
        Contents oldContents, newContents;
        do
        {
            if ( null == ( oldContents = cache.get() ) )
            {
                return false;
            }
            synchronized ( oldContents )
            {
                int index;
                for ( index = 0; index < oldContents.size; index++ )
                {
                    if ( element.equals( oldContents.objs[index] ) )
                    {
                        break;
                    }
                }
                if ( index >= oldContents.size )
                {
                    return false;
                }
                newContents = remove( oldContents, index );
            }
        }
        while ( oldContents != newContents && !cache.compareAndSet( oldContents, newContents ) );

        return true;
    }

    public boolean removeThis( final T element )
    {
        Contents oldContents, newContents;
        do
        {
            if ( null == ( oldContents = cache.get() ) )
            {
                return false;
            }
            synchronized ( oldContents )
            {
                int index;
                for ( index = 0; index < oldContents.size; index++ )
                {
                    if ( element == oldContents.objs[index] )
                    {
                        break;
                    }
                }
                if ( index >= oldContents.size )
                {
                    return false;
                }
                newContents = remove( oldContents, index );
            }
        }
        while ( oldContents != newContents && !cache.compareAndSet( oldContents, newContents ) );

        return true;
    }

    @Override
    public Object[] toArray()
    {
        final Contents contents = cache.get();
        if ( null != contents )
        {
            final Object[] elements = new Object[contents.size];
            System.arraycopy( contents.objs, 0, elements, 0, elements.length );
            return elements;
        }
        return NO_ELEMENTS;
    }

    public void clear()
    {
        cache.set( null );
    }

    @Override
    public boolean isEmpty()
    {
        return null == cache.get();
    }

    public int size()
    {
        final Contents contents = cache.get();
        return null == contents ? 0 : contents.size;
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
     * @param uids The UIDs array
     * @param size The UIDs size
     * @param uid The UID to find
     * @return Index with nearest UID
     */
    static int safeBinarySearch( final long[] uids, final int size, final long uid )
    {
        if ( uid < uids[0] )
        {
            return 0;
        }
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

    static <T> Contents insert( final Contents oldContents, final T element, final int rank )
    {
        final Contents newContents;
        if ( oldContents.isImmutable )
        {
            newContents = new Contents( oldContents );
        }
        else
        {
            ( newContents = oldContents ).ensureCapacity();
        }

        final long uid = rank2uid( rank, newContents.uniq++ );
        final int index = safeBinarySearch( newContents.uids, newContents.size, uid );

        final int destPos = index + 1, len = newContents.size - index;
        if ( len > 0 )
        {
            System.arraycopy( newContents.objs, index, newContents.objs, destPos, len );
            System.arraycopy( newContents.uids, index, newContents.uids, destPos, len );
        }
        newContents.size++;

        newContents.objs[index] = element;
        newContents.uids[index] = uid;

        return newContents;
    }

    static Contents remove( final Contents oldContents, final int index )
    {
        if ( index == 0 && oldContents.size == 1 )
        {
            return null;
        }

        final Contents newContents;
        if ( oldContents.isImmutable )
        {
            newContents = new Contents( oldContents );
        }
        else
        {
            newContents = oldContents;
        }

        final int from = index + 1, len = newContents.size - from;
        if ( len > 0 )
        {
            System.arraycopy( newContents.objs, from, newContents.objs, index, len );
            System.arraycopy( newContents.uids, from, newContents.uids, index, len );
        }
        newContents.objs[--newContents.size] = null; // remove dangling reference

        return newContents;
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
                copyAndExpand( contents );
            }
            else
            {
                objs = contents.objs.clone();
                uids = contents.uids.clone();
            }
        }

        void ensureCapacity()
        {
            if ( size >= objs.length )
            {
                copyAndExpand( this );
            }
        }

        private void copyAndExpand( final Contents contents )
        {
            final int capacity = size * 3 / 2 + 1;

            final Object[] newObjs = new Object[capacity];
            System.arraycopy( contents.objs, 0, newObjs, 0, size );

            final long[] newUIDs = new long[capacity];
            System.arraycopy( contents.uids, 0, newUIDs, 0, size );

            objs = newObjs;
            uids = newUIDs;
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

        private T nextObj, element;

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
                element = nextObj;
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
            if ( null == element )
            {
                throw new IllegalStateException();
            }
            RankedSequence.this.removeThis( element );
            element = null;
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
                if ( newContents.isImmutable )
                {
                    index = safeBinarySearch( newContents.uids, newContents.size, nextUID );
                }
                else
                {
                    synchronized ( contents )
                    {
                        newContents.isImmutable = true;
                        index = safeBinarySearch( newContents.uids, newContents.size, nextUID );
                    }
                }
            }
            return null != newContents && index < newContents.size;
        }
    }
}
