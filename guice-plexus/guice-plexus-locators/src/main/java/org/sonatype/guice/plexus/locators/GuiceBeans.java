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

import java.util.Map.Entry;

import com.google.inject.Injector;

/**
 * Dynamic {@link Iterable} sequence of beans backed by bindings from zero or more Guice {@link Injector}s.
 */
interface GuiceBeans<T>
    extends Iterable<Entry<String, T>>
{
    /**
     * Adds the given Guice {@link Injector} to the backing list.
     * 
     * @param injector The Guice injector
     * @return {@code true} if the sequence changed as a result of the call; otherwise {@code false}
     */
    boolean add( Injector injector );

    /**
     * Removes the given Guice {@link Injector} from the backing list.
     * 
     * @param injector The Guice injector
     * @return {@code true} if the sequence changed as a result of the call; otherwise {@code false}
     */
    boolean remove( Injector injector );
}
