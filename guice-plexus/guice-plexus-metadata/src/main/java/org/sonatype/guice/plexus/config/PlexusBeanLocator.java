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
package org.sonatype.guice.plexus.config;

import java.util.Map.Entry;

import org.sonatype.guice.bean.reflect.DeferredClass;

import com.google.inject.ImplementedBy;
import com.google.inject.TypeLiteral;

/**
 * Service that locates beans of various types, using optional Plexus hints as a guide.
 */
@ImplementedBy( MutablePlexusBeanLocator.class )
public interface PlexusBeanLocator
{
    /**
     * Plexus bean mapping; from hint->instance.
     */
    interface Bean<T>
        extends Entry<String, T>
    {
        /**
         * @return Human readable bean description
         */
        String getDescription();

        /**
         * @return Deferred implementation class
         */
        DeferredClass<T> getImplementationClass();
    }

    /**
     * Locates beans of the given type, optionally filtered using the given named hints.
     * 
     * @param role The expected bean type
     * @param hints The optional hints
     * @return Sequence of Plexus bean mappings; ordered according to the given hints
     */
    <T> Iterable<Bean<T>> locate( TypeLiteral<T> role, String... hints );
}