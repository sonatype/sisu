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
 * Represents an {@link Iterable} sequence of items that can be watched.
 */
public interface Watchable<T>
    extends Iterable<T>
{
    /**
     * Subscribes the given {@link Watcher} to receive updates from this sequence.
     * 
     * @param watcher The sequence watcher
     * @return {@code true} if the watcher was newly subscribed; otherwise {@code false}
     */
    boolean subscribe( Watcher<T> watcher );

    /**
     * Unsubscribes the given {@link Watcher} from receiving updates to this sequence.
     * 
     * @param watcher The sequence watcher
     * @return {@code true} if the watcher was unsubscribed; otherwise {@code false}
     */
    boolean unsubscribe( Watcher<T> watcher );
}
