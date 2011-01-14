/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.locators.spi;

import com.google.inject.Binding;

/**
 * Distributor of {@link Binding}s retrieved from a series of {@link BindingPublisher}s.
 */
public interface BindingDistributor
{
    /**
     * Adds the given ranked {@link BindingPublisher} and distributes its {@link Binding}s.
     * 
     * @param publisher The new publisher
     * @param rank The assigned rank
     */
    void add( BindingPublisher publisher, int rank );

    /**
     * Removes the given {@link BindingPublisher} and its {@link Binding}s.
     * 
     * @param publisher The old publisher
     */
    void remove( BindingPublisher publisher );

    /**
     * Removes all known {@link BindingPublisher}s and their {@link Binding}s.
     */
    void clear();
}
