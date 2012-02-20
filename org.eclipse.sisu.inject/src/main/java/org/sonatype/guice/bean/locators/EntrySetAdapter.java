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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link Set} backed by an {@link Iterable} sequence of map entries.
 */
public final class EntrySetAdapter<K, V>
    extends AbstractSet<V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<? extends Entry<K, V>> iterable;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public EntrySetAdapter( final Iterable<? extends Entry<K, V>> iterable )
    {
        this.iterable = iterable;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<V> iterator()
    {
        return new ValueIterator<K, V>( iterable );
    }

    @Override
    public boolean isEmpty()
    {
        return false == iterator().hasNext();
    }

    @Override
    public int size()
    {
        int size = 0;
        for ( final Iterator<?> i = iterable.iterator(); i.hasNext(); i.next() )
        {
            size++;
        }
        return size;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Value {@link Iterator} backed by a Key:Value {@link Iterator}.
     */
    private static final class ValueIterator<K, V>
        implements Iterator<V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<? extends Entry<K, V>> iterator;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ValueIterator( final Iterable<? extends Entry<K, V>> iterable )
        {
            this.iterator = iterable.iterator();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        public V next()
        {
            return iterator.next().getValue();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
