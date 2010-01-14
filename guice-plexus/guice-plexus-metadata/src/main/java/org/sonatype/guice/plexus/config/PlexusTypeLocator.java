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

import com.google.inject.TypeLiteral;

/**
 * Locates instances of various types, using optional Plexus hints as a guide.
 */
public interface PlexusTypeLocator
{
    /**
     * Locates instances of the given type, optionally filtered using the given named hints.
     * 
     * @param type The expected type
     * @param hints The optional hints
     * @return Sequence of hint->instance map entries; ordered according to the given hints
     */
    <T> Iterable<Entry<String, T>> locate( TypeLiteral<T> type, String... hints );
}