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
package org.sonatype.inject;

import javax.inject.Named;
import javax.inject.Provider;

/**
 * Mediates {@link Named} bean updates between the {@code BeanLocator} and an associated watcher.
 */
public interface NamedBeanMediator<T, W>
{
    /**
     * Notifies the associated watcher that a {@link Named} bean has been added.
     * 
     * @param name The bean name
     * @param bean The bean provider
     * @param watcher The associated watcher
     */
    void add( String name, Provider<T> bean, W watcher )
        throws Exception;

    /**
     * Notifies the associated watcher that a {@link Named} bean has been removed.
     * 
     * @param name The bean name
     * @param bean The bean provider
     * @param watcher The associated watcher
     */
    void remove( String name, Provider<T> bean, W watcher )
        throws Exception;
}
