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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ordered {@link List} that arranges elements by descending rank; supports concurrent iteration and modification.
 */
final class RankedSequence<T>
    extends AbstractCollection<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final AtomicReference<Contents> cache = new AtomicReference<Contents>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RankedSequence()
    {
        // nothing to do
    }

    RankedSequence( final RankedSequence<T> sequence )
    {
        final Contents contents;
        if ( null != sequence && null != ( contents = sequence.cache.get() ) )
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
            else if ( oldContents.isImmutable )
            {
                newContents = oldContents.insert( element, rank );
            }
            else
            {
                synchronized ( oldContents )
                {
                    newContents = oldContents.insert( element, rank );
                }
            }
        }
        while ( oldContents != newContents && !cache.compareAndSet( oldContents, newContents ) );
    }

    @Override
    public boolean add( final T element )
    {
        insert( element, 0 );
        return true;
    }

    public void optimizeForReading()
    {
        final Contents contents = cache.get();
        if ( null != contents && !contents.isImmutable )
        {
            synchronized ( contents )
            {
                contents.isImmutable = true; // optimize for reading
            }
        }
    }

    @Override
    public boolean contains( final Object element )
    {
        final Contents contents = cache.get();
        if ( null == contents )
        {
            return false;
        }
        else if ( contents.isImmutable )
        {
            return contents.indexOf( element ) >= 0;
        }
        synchronized ( contents )
        {
            return contents.indexOf( element ) >= 0;
        }
    }

    public boolean containsThis( final Object element )
    {
        final Contents contents = cache.get();
        if ( null == contents )
        {
            return false;
        }
        else if ( contents.isImmutable )
        {
            return contents.indexOfThis( element ) >= 0;
        }
        synchronized ( contents )
        {
            return contents.indexOfThis( element ) >= 0;
        }
    }

    @Override
    public boolean remove( final Object element )
    {
        Contents oldContents, newContents;
        do
        {
            if ( null == ( oldContents = cache.get() ) )
            {
                return false;
            }
            else if ( oldContents.isImmutable )
            {
                final int index = oldContents.indexOf( element );
                if ( index < 0 )
                {
                    return false;
                }
                newContents = oldContents.remove( index );
            }
            else
            {
                synchronized ( oldContents )
                {
                    final int index = oldContents.indexOf( element );
                    if ( index < 0 )
                    {
                        return false;
                    }
                    newContents = oldContents.remove( index );
                }
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
            else if ( oldContents.isImmutable )
            {
                final int index = oldContents.indexOfThis( element );
                if ( index < 0 )
                {
                    return false;
                }
                newContents = oldContents.remove( index );
            }
            else
            {
                synchronized ( oldContents )
                {
                    final int index = oldContents.indexOfThis( element );
                    if ( index < 0 )
                    {
                        return false;
                    }
                    newContents = oldContents.remove( index );
                }
            }
        }
        while ( oldContents != newContents && !cache.compareAndSet( oldContents, newContents ) );

        return true;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public Iterable<T> snapshot()
    {
        final Contents contents = cache.get();
        if ( null == contents )
        {
            return Collections.EMPTY_SET;
        }
        else if ( contents.isImmutable )
        {
            final Object[] elements = new Object[contents.size];
            System.arraycopy( contents.objs, 0, elements, 0, elements.length );
            return (List) Arrays.asList( elements );
        }
        synchronized ( contents )
        {
            final Object[] elements = new Object[contents.size];
            System.arraycopy( contents.objs, 0, elements, 0, elements.length );
            return (List) Arrays.asList( elements );
        }
    }

    @Override
    public void clear()
    {
        cache.set( null );
    }

    @Override
    public boolean isEmpty()
    {
        return null == cache.get();
    }

    @Override
    public int size()
    {
        final Contents contents = cache.get();
        return null == contents ? 0 : contents.size;
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

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class Contents
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

        int size;

        int uniq;

        volatile boolean isImmutable;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Contents()
        {
            // blank template
        }

        Contents( final Object element, final int rank )
        {
            objs = new Object[INITIAL_CAPACITY];
            uids = new long[INITIAL_CAPACITY];

            objs[0] = element;
            uids[0] = rank2uid( rank, uniq );

            size++;
            uniq++;
        }

        Contents( final Contents contents )
        {
            size = contents.size;
            uniq = contents.uniq;

            if ( contents.isImmutable )
            {
                // can share the data
                objs = contents.objs;
                uids = contents.uids;

                isImmutable = true;
            }
            else
            {
                objs = contents.objs.clone();
                uids = contents.uids.clone();
            }
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public int indexOf( final Object element )
        {
            for ( int i = 0; i < size; i++ )
            {
                if ( element.equals( objs[i] ) )
                {
                    return i;
                }
            }
            return -1;
        }

        public int indexOfThis( final Object element )
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

        public Contents insert( final Object element, final int rank )
        {
            final long uid = rank2uid( rank, uniq );
            final int index = safeBinarySearch( uids, size, uid );

            final Object[] newObjs;
            final long[] newUIDs;

            if ( size >= objs.length )
            {
                final int capacity = size * 3 / 2 + 1;

                newObjs = new Object[capacity];
                newUIDs = new long[capacity];

                if ( index > 0 )
                {
                    System.arraycopy( objs, 0, newObjs, 0, index );
                    System.arraycopy( uids, 0, newUIDs, 0, index );
                }
            }
            else if ( isImmutable )
            {
                newObjs = objs.clone();
                newUIDs = uids.clone();
            }
            else
            {
                newObjs = objs;
                newUIDs = uids;
            }

            final int destPos = index + 1, len = size - index;
            if ( len > 0 )
            {
                System.arraycopy( objs, index, newObjs, destPos, len );
                System.arraycopy( uids, index, newUIDs, destPos, len );
            }

            newObjs[index] = element;
            newUIDs[index] = uid;

            final Contents newContents = isImmutable ? new Contents() : this;

            newContents.objs = newObjs;
            newContents.uids = newUIDs;

            newContents.size = size + 1;
            newContents.uniq = uniq + 1;

            return newContents;
        }

        public Contents remove( final int index )
        {
            if ( index == 0 && size == 1 )
            {
                return null;
            }

            final Object[] newObjs;
            final long[] newUIDs;

            if ( isImmutable )
            {
                newObjs = objs.clone();
                newUIDs = uids.clone();
            }
            else
            {
                newObjs = objs;
                newUIDs = uids;
            }

            final int srcPos = index + 1, len = size - srcPos;
            if ( len > 0 )
            {
                System.arraycopy( objs, srcPos, newObjs, index, len );
                System.arraycopy( uids, srcPos, newUIDs, index, len );
            }

            final Contents newContents = isImmutable ? new Contents() : this;

            newContents.objs = newObjs;
            newContents.uids = newUIDs;

            newContents.size = size - 1;
            newContents.uniq = uniq;

            newContents.objs[newContents.size] = null; // remove dangling reference

            return newContents;
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

        private int index;

        private int expectedSize, expectedUniq;

        private long nextUID = Long.MIN_VALUE;

        private T nextObj, element;

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
            final Contents newContents = cache.get();
            if ( null == newContents )
            {
                expectedSize = 0;
                expectedUniq = 0;
            }
            else if ( newContents.isImmutable )
            {
                sync( newContents );
                if ( index < newContents.size )
                {
                    nextObj = (T) newContents.objs[index];
                    nextUID = newContents.uids[index];
                    return true;
                }
            }
            else
            {
                synchronized ( newContents )
                {
                    sync( newContents );
                    if ( index < newContents.size )
                    {
                        nextObj = (T) newContents.objs[index];
                        nextUID = newContents.uids[index];
                        return true;
                    }
                }
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
            final Contents contents = cache.get();
            if ( null == contents )
            {
                expectedSize = 0;
                expectedUniq = 0;
            }
            else if ( contents.isImmutable )
            {
                sync( contents );
                if ( index < contents.size )
                {
                    return uid2rank( contents.uids[index] );
                }
            }
            else
            {
                synchronized ( contents )
                {
                    sync( contents );
                    if ( index < contents.size )
                    {
                        return uid2rank( contents.uids[index] );
                    }
                }
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
            if ( null != element )
            {
                RankedSequence.this.removeThis( element );
                element = null;
            }
            else
            {
                throw new IllegalStateException();
            }
        }

        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        private void sync( final Contents contents )
        {
            if ( expectedSize != contents.size || expectedUniq != contents.uniq )
            {
                // sequence has been modified, so we may need to reposition index
                index = safeBinarySearch( contents.uids, contents.size, nextUID );
                expectedSize = contents.size;
                expectedUniq = contents.uniq;
            }
        }
    }
}
