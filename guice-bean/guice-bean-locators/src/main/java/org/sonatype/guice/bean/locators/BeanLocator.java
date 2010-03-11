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

import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import javax.inject.Qualifier;

import com.google.inject.Key;

/**
 * Locates beans from some central registry by using qualified {@link Key}s.
 */
public interface BeanLocator
{
    /**
     * Locates any beans that match the given {@link Key}.
     * 
     * @param key The key; with optional {@link Qualifier}
     * @return Sequence of qualifier to bean mappings
     */
    <Q extends Annotation, T> Iterable<Entry<Q, T>> locate( Key<T> key );
}
