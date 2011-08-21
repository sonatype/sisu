/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link Collection} of elements kept alive by soft/weak {@link Reference}s; automatically compacts on read/write.
 * <p>
 * Note: this class is not synchronized and all methods (including iterators) may silently remove elements.
 */
final class MildElements<T>
    extends AbstractCollection<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ReferenceQueue<T> queue = new ReferenceQueue<T>();

    final List<Reference<T>> list;

    private final boolean soft;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildElements( final List<Reference<T>> list, final boolean soft )
    {
        this.list = list;
        this.soft = soft;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public boolean add( final T element )
    {
        compact();

        return list.add( mild( element ) );
    }

    @Override
    public int size()
    {
        compact();

        return list.size();
    }

    @Override
    public Iterator<T> iterator()
    {
        compact();

        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Soft or weak {@link Reference} item for the given element.
     */
    private Reference<T> mild( final T element )
    {
        return soft ? new Soft<T>( element, queue, list.size() ) : new Weak<T>( element, queue, list.size() );
    }

    /**
     * Compacts the list by replacing cleared items with ones from the end.
     */
    @SuppressWarnings( "unchecked" )
    private void compact()
    {
        Reference<? extends T> ref;
        while ( ( ref = queue.poll() ) != null )
        {
            ( (Item<Reference<T>>) ref ).compact( list );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Common functionality shared by both soft and weak items in the list.
     */
    private interface Item<T>
    {
        /**
         * Compacts the list by replacing this item with one from the end of the list.
         * 
         * @param list The containing list
         */
        void compact( List<T> list );
    }

    /**
     * {@link Iterator} that iterates over uncleared {@link Reference}s in the list.
     */
    final class Itr
        implements Iterator<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        private T nextElement;

        private boolean haveElement;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            // find the next element that has yet to be cleared
            while ( null == nextElement && index < list.size() )
            {
                nextElement = list.get( index++ ).get();
            }
            return null != nextElement;
        }

        public T next()
        {
            haveElement = hasNext();
            if ( haveElement )
            {
                // populated by hasNext()
                final T element = nextElement;
                nextElement = null;
                return element;
            }
            throw new NoSuchElementException();
        }

        @SuppressWarnings( "unchecked" )
        public void remove()
        {
            if ( haveElement )
            {
                // backtrack to previous position and remove it from the list
                ( (Item<Reference<T>>) list.get( --index ) ).compact( list );
                haveElement = false;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Soft {@link Item} that keeps track of its index so it can be swapped out.
     */
    private static final class Soft<T>
        extends SoftReference<T>
        implements Item<Soft<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Soft( final T value, final ReferenceQueue<T> queue, final int index )
        {
            super( value, queue );
            this.index = index;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void compact( final List<Soft<T>> list )
        {
            if ( index >= 0 )
            {
                // swap the last element in the list into our old position
                final Soft<T> lastElement = list.remove( list.size() - 1 );
                if ( this != lastElement )
                {
                    lastElement.index = index;
                    list.set( index, lastElement );
                }
                index = -1;
            }
        }
    }

    /**
     * Weak {@link Item} that keeps track of its index so it can be swapped out.
     */
    private static final class Weak<T>
        extends WeakReference<T>
        implements Item<Weak<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Weak( final T value, final ReferenceQueue<T> queue, final int index )
        {
            super( value, queue );
            this.index = index;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void compact( final List<Weak<T>> list )
        {
            if ( index >= 0 )
            {
                // swap the last element in the list into our old position
                final Weak<T> lastElement = list.remove( list.size() - 1 );
                if ( this != lastElement )
                {
                    lastElement.index = index;
                    list.set( index, lastElement );
                }
                index = -1;
            }
        }
    }
}
