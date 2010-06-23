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

import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Injector;

/**
 * Dynamic {@link Iterable} sequence of beans backed by bindings from zero or more {@link Injector}s.
 */
interface PlexusBeans<T>
    extends Iterable<PlexusBeanLocator.Bean<T>>
{
    /**
     * Adds bindings from the given {@link Injector} to the backing list.
     * 
     * @param injector The new injector
     * @return {@code true} if the sequence changed as a result of the call; otherwise {@code false}
     */
    boolean add( Injector injector );

    /**
     * Removes bindings from the given {@link Injector} from the backing list.
     * 
     * @param injector The old injector
     * @return {@code true} if the sequence changed as a result of the call; otherwise {@code false}
     */
    boolean remove( Injector injector );

    /**
     * Removes all bindings from the backing list.
     */
    void clear();
}
