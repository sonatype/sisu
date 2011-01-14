/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.inject.name.Named;

/**
 * String mapping {@link Iterable} backed by a {@link Named} mapping {@link Iterable}.
 */
public final class NamedIterableAdapter<V>
    implements Iterable<Entry<String, V>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Entry<Named, V>> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public NamedIterableAdapter( final Iterable<Entry<Named, V>> delegate )
    {
        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<Entry<String, V>> iterator()
    {
        return new NamedIterator<V>( delegate );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * String mapping {@link Iterator} backed by a {@link Named} mapping {@link Iterator}.
     */
    private static final class NamedIterator<V>
        implements Iterator<Entry<String, V>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Entry<Named, V>> iterator;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedIterator( final Iterable<Entry<Named, V>> iterable )
        {
            iterator = iterable.iterator();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        public Entry<String, V> next()
        {
            return new NamedEntry<V>( iterator.next() );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * String mapping {@link Entry} backed by a {@link Named} mapping {@link Entry}.
     */
    private static final class NamedEntry<V>
        implements Entry<String, V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Entry<Named, V> entry;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedEntry( final Entry<Named, V> entry )
        {
            this.entry = entry;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public String getKey()
        {
            return entry.getKey().value();
        }

        public V getValue()
        {
            return entry.getValue();
        }

        public V setValue( final V value )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString()
        {
            return getKey() + "=" + getValue();
        }
    }
}
