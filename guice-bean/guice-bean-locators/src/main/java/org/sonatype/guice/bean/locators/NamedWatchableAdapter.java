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

import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Named;

/**
 * String mapping {@link Watchable} backed by a {@link Named} mapping {@link Watchable}.
 */
public final class NamedWatchableAdapter<V>
    implements Watchable<Entry<String, V>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Watchable<Entry<Named, V>> watchable;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public NamedWatchableAdapter( final Watchable<Entry<Named, V>> watchable )
    {
        this.watchable = watchable;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<Entry<String, V>> iterator()
    {
        return new NamedIteratorAdapter<V>( watchable );
    }

    public Watcher<Entry<String, V>> subscribe( final Watcher<Entry<String, V>> watcher )
    {
        final NamedWatcherAdapter<V> adapter =
            (NamedWatcherAdapter<V>) watchable.subscribe( new NamedWatcherAdapter<V>( watcher ) );

        return null != adapter ? adapter.watcher : null;
    }

    public boolean unsubscribe( final Watcher<Entry<String, V>> watcher )
    {
        return watchable.unsubscribe( new NamedWatcherAdapter<V>( watcher ) );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * String mapping {@link Iterator} backed by a {@link Named} mapping {@link Iterator}.
     */
    private static final class NamedIteratorAdapter<V>
        implements Iterator<Entry<String, V>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Entry<Named, V>> iterator;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedIteratorAdapter( final Iterable<Entry<Named, V>> iterable )
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
            return new NamedEntryAdapter<V>( iterator.next() );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * String mapping {@link Entry} backed by a {@link Named} mapping {@link Entry}.
     */
    private static final class NamedEntryAdapter<V>
        implements Entry<String, V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Entry<Named, V> entry;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedEntryAdapter( final Entry<Named, V> entry )
        {
            this.entry = entry;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public String getKey()
        {
            final Named named = entry.getKey();
            return null != named ? named.value() : null;
        }

        public V getValue()
        {
            return entry.getValue();
        }

        public V setValue( final V value )
        {
            throw new UnsupportedOperationException();
        }

        // TODO: equals
    }

    private static final class NamedWatcherAdapter<V>
        implements Watcher<Entry<Named, V>>
    {
        final Watcher<Entry<String, V>> watcher;

        NamedWatcherAdapter( final Watcher<Entry<String, V>> watcher )
        {
            this.watcher = watcher;
        }

        public void add( final Entry<Named, V> item )
        {
            watcher.add( new NamedEntryAdapter<V>( item ) );
        }

        public void remove( final Entry<Named, V> item )
        {
            watcher.remove( new NamedEntryAdapter<V>( item ) );
        }

        // TODO: equals
    }
}
