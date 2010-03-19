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

/**
 * Something that watches a {@link Watchable} sequence of items.
 */
public interface Watcher<T>
{
    /**
     * Notifies the watcher that the given item was added to the end of the watched sequence.
     * 
     * @param item The added item
     */
    void add( T item );

    /**
     * Notifies the watcher that the given item was removed from the watched sequence.
     * 
     * @param item The removed item
     */
    void remove( T item );
}
