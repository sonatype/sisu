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

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;

/**
 * Mutable {@link BeanLocator} that tracks zero or more {@link Injector}s.
 */
@ImplementedBy( DefaultBeanLocator.class )
public interface MutableBeanLocator
    extends BeanLocator
{
    /**
     * Adds qualified beans belonging to the given injector to any exposed/watched sequences.
     * 
     * @param injector The new injector
     */
    void add( Injector injector );

    /**
     * Removes qualified beans belonging to the given injector from any exposed/watched sequences.
     * 
     * @param injector The old injector
     */
    void remove( Injector injector );

    /**
     * Removes all known qualified beans from any exposed/watched sequences.
     */
    void clear();
}
