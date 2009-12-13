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
package org.sonatype.guice.plexus.binders;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

final class IterableMapAdapter<T>
    extends AbstractMap<String, T>
{
    private final Set<Entry<String, T>> entrySet;

    IterableMapAdapter( final Iterable<Entry<String, T>> iterable )
    {
        entrySet = new AbstractSet<Entry<String, T>>()
        {
            @Override
            public Iterator<Entry<String, T>> iterator()
            {
                return iterable.iterator();
            }

            @Override
            public int size()
            {
                final Iterator<Entry<String, T>> i = iterable.iterator();

                int size = 0;
                while ( i.hasNext() )
                {
                    i.next();
                    size++;
                }

                return size;
            }
        };
    }

    @Override
    public Set<Entry<String, T>> entrySet()
    {
        return entrySet;
    }
}