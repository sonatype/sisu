/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.locators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * 
 */
abstract class AbstractBeanEntries<T>
    implements Iterable<Entry<String, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final List<Injector> injectors;

    final TypeLiteral<T> role;

    private List<Entry<String, T>> cache;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    AbstractBeanEntries( final List<Injector> injectors, final TypeLiteral<T> role )
    {
        this.injectors = injectors;
        this.role = role;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final Iterator<Entry<String, T>> iterator()
    {
        return new CachingIterator();
    }

    // ----------------------------------------------------------------------
    // Cache methods
    // ----------------------------------------------------------------------

    abstract Entry<String, T> populate( final int index );

    final boolean cache( final Entry<String, T> entry )
    {
        if ( null == cache )
        {
            cache = new ArrayList<Entry<String, T>>();
        }
        return cache.add( entry );
    }

    synchronized boolean hasNext( final int index )
    {
        if ( null != cache && index < cache.size() )
        {
            return true;
        }
        final Entry<String, T> entry = populate( index );
        if ( null == entry )
        {
            return false;
        }
        if ( null == cache )
        {
            cache = new ArrayList<Entry<String, T>>();
        }
        return cache.add( entry );
    }

    synchronized Entry<String, T> next( final int index )
    {
        if ( hasNext( index ) )
        {
            return cache.get( index );
        }
        throw new NoSuchElementException();
    }

    // ----------------------------------------------------------------------
    // Iterator implementation
    // ----------------------------------------------------------------------

    final class CachingIterator
        implements Iterator<Entry<String, T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return AbstractBeanEntries.this.hasNext( index );
        }

        public Entry<String, T> next()
        {
            return AbstractBeanEntries.this.next( index++ );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
