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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple collection of elements held by {@link WeakReference}s; automatically compacts on read/write.
 */
final class WeakSequence<T>
    extends AbstractCollection<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<IndexedReference<T>> refs = new ArrayList<IndexedReference<T>>();

    private final ReferenceQueue<T> queue = new ReferenceQueue<T>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public synchronized boolean add( final T value )
    {
        compact();

        return refs.add( new IndexedReference<T>( value, queue, refs.size() ) );
    }

    public synchronized <L> boolean link( final T value, final L link )
    {
        compact();

        return refs.add( new LinkedReference<T, L>( value, queue, refs.size(), link ) );
    }

    @Override
    public synchronized Iterator<T> iterator()
    {
        compact();

        final int size = refs.size();
        final List<T> elements = new ArrayList<T>( size );
        for ( int i = 0; i < size; i++ )
        {
            final T e = refs.get( i ).get();
            if ( null != e )
            {
                elements.add( e );
            }
        }

        return elements.iterator();
    }

    @Override
    public synchronized int size()
    {
        return refs.size();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Compacts the collection by replacing evicted elements with ones from the end.
     */
    private void compact()
    {
        Reference<? extends T> ref;
        while ( ( ref = queue.poll() ) instanceof IndexedReference<?> )
        {
            @SuppressWarnings( "unchecked" )
            final int index = ( (IndexedReference<T>) ref ).index;
            final IndexedReference<T> lastRef = refs.remove( refs.size() - 1 );
            if ( index != lastRef.index )
            {
                lastRef.index = index;
                refs.set( index, lastRef );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link WeakReference} that remembers its position so it can be quickly evicted.
     */
    private static class IndexedReference<T>
        extends WeakReference<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        IndexedReference( final T value, final ReferenceQueue<T> queue, final int index )
        {
            super( value, queue );
            this.index = index;
        }
    }

    private static final class LinkedReference<T, L>
        extends IndexedReference<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        T value;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        LinkedReference( final T value, final ReferenceQueue<T> queue, final int index, final L link )
        {
            super( (T) link, queue, index );
            this.value = value;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public T get()
        {
            return value;
        }
    }
}
