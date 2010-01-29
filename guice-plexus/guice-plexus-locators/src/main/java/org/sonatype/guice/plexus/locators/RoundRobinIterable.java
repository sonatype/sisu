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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 
 */
final class RoundRobinIterable<T>
    implements Iterable<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<T> componentType;

    private final Iterable<?>[] iterables;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RoundRobinIterable( final Class<T> componentType, final List<Iterable<?>> iterables )
    {
        this.componentType = componentType;
        this.iterables = iterables.toArray( new Iterable[iterables.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<T> iterator()
    {
        return new RoundRobinIterator<T>( componentType, iterables );
    }

    // ----------------------------------------------------------------------
    // Iterator implementation
    // ----------------------------------------------------------------------

    private static final class RoundRobinIterator<T>
        implements Iterator<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Class<T> componentType;

        private final Iterator<?>[] iterators;

        private Object nextValue;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RoundRobinIterator( final Class<T> componentType, final Iterable<?>... iterables )
        {
            this.componentType = componentType;
            iterators = new Iterator[iterables.length];
            for ( int i = 0; i < iterators.length; i++ )
            {
                iterators[i] = iterables[i].iterator();
            }
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            for ( final Iterator<?> iterator : iterators )
            {
                if ( componentType.isInstance( nextValue ) )
                {
                    return true;
                }
                if ( iterator.hasNext() )
                {
                    nextValue = iterator.next();
                }
            }
            return false;
        }

        @SuppressWarnings( "unchecked" )
        public T next()
        {
            if ( hasNext() )
            {
                return (T) nextValue;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}