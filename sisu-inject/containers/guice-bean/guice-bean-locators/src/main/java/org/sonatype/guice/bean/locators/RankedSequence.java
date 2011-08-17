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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ordered {@link List} that arranges elements by descending rank; supports concurrent iteration and modification.
 */
final class RankedSequence<T>
    implements Iterable<T>
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
        if ( null != sequence )
        {
            cache.set( sequence.cache.get() );
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
        Contents o, n;
        do
        {
            n = null != ( o = cache.get() ) ? o.insert( element, rank ) : new Contents( element, rank );
        }
        while ( !cache.compareAndSet( o, n ) );
    }

    @SuppressWarnings( "unchecked" )
    public T poll()
    {
        final Contents contents = cache.get();
        cache.set( contents.remove( 0 ) );
        return (T) contents.objs[0];
    }

    public int topRank()
    {
        final Contents contents = cache.get();
        return null != contents ? uid2rank( contents.uids[0] ) : Integer.MIN_VALUE;
    }

    public boolean contains( final Object element )
    {
        final Contents contents = cache.get();
        return null != contents && contents.indexOf( element ) >= 0;
    }

    public boolean containsThis( final Object element )
    {
        final Contents contents = cache.get();
        return null != contents && contents.indexOfThis( element ) >= 0;
    }

    public boolean remove( final Object element )
    {
        Contents o, n;
        do
        {
            final int index;
            if ( null == ( o = cache.get() ) || ( index = o.indexOf( element ) ) < 0 )
            {
                return false;
            }
            n = o.remove( index );
        }
        while ( !cache.compareAndSet( o, n ) );

        return true;
    }

    public boolean removeThis( final T element )
    {
        Contents o, n;
        do
        {
            final int index;
            if ( null == ( o = cache.get() ) || ( index = o.indexOfThis( element ) ) < 0 )
            {
                return false;
            }
            n = o.remove( index );
        }
        while ( !cache.compareAndSet( o, n ) );

        return true;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public Iterable<T> snapshot()
    {
        final Contents contents = cache.get();
        return null != contents ? (List) Arrays.asList( contents.objs ) : Collections.EMPTY_SET;
    }

    public void clear()
    {
        cache.set( null );
    }

    public boolean isEmpty()
    {
        return null == cache.get();
    }

    public int size()
    {
        final Contents contents = cache.get();
        return null != contents ? contents.objs.length : 0;
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
     * @param uid The UID to find
     * @return Index with nearest UID
     */
    static int safeBinarySearch( final long[] uids, final long uid )
    {
        if ( uid < uids[0] )
        {
            return 0;
        }
        int min = 0;
        int max = uids.length - 1;
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
        if ( min == uids.length - 1 && uids[min] < uid )
        {
            min++; // append
        }
        return min;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class Contents
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        final Object[] objs;

        final long[] uids;

        final int uniq;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Contents( final Object element, final int rank )
        {
            objs = new Object[] { element };
            uids = new long[] { rank2uid( rank, 0 ) };
            uniq = 1;
        }

        Contents( final Object[] objs, final long[] uids, final int uniq )
        {
            this.objs = objs;
            this.uids = uids;
            this.uniq = uniq;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public int indexOf( final Object element )
        {
            for ( int i = 0; i < objs.length; i++ )
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
            for ( int i = 0; i < objs.length; i++ )
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
            final int size = objs.length + 1;

            final Object[] newObjs = new Object[size];
            final long[] newUIDs = new long[size];

            final long uid = rank2uid( rank, uniq );
            final int index = safeBinarySearch( uids, uid );
            if ( index > 0 )
            {
                System.arraycopy( objs, 0, newObjs, 0, index );
                System.arraycopy( uids, 0, newUIDs, 0, index );
            }

            newObjs[index] = element;
            newUIDs[index] = uid;

            final int destPos = index + 1, len = size - destPos;
            if ( len > 0 )
            {
                System.arraycopy( objs, index, newObjs, destPos, len );
                System.arraycopy( uids, index, newUIDs, destPos, len );
            }

            return new Contents( newObjs, newUIDs, uniq + 1 );
        }

        public Contents remove( final int index )
        {
            if ( objs.length == 1 )
            {
                return null;
            }

            final int size = objs.length - 1;

            final Object[] newObjs = new Object[size];
            final long[] newUIDs = new long[size];

            if ( index > 0 )
            {
                System.arraycopy( objs, 0, newObjs, 0, index );
                System.arraycopy( uids, 0, newUIDs, 0, index );
            }
            final int srcPos = index + 1, len = size - index;
            if ( len > 0 )
            {
                System.arraycopy( objs, srcPos, newObjs, index, len );
                System.arraycopy( uids, srcPos, newUIDs, index, len );
            }

            return new Contents( newObjs, newUIDs, uniq );
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

        private T nextObj;

        private long nextUID = Long.MIN_VALUE;

        private int index = -1;

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
            if ( contents != newContents )
            {
                index = null != newContents ? safeBinarySearch( newContents.uids, nextUID ) : -1;
                contents = newContents;
            }
            if ( index >= 0 && index < contents.objs.length )
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
            final Contents newContents = cache.get();
            if ( contents != newContents )
            {
                index = null != newContents ? safeBinarySearch( newContents.uids, nextUID ) : -1;
                contents = newContents;
            }
            if ( index >= 0 && index < contents.objs.length )
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
    }
}
