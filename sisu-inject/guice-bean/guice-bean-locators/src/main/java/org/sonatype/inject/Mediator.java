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

import java.lang.annotation.Annotation;

/**
 * Mediates bean entries sent from the {@code BeanLocator} to associated watching objects.
 */
public interface Mediator<Q extends Annotation, T, W>
{
    /**
     * Processes the added {@link BeanEntry} and sends the necessary updates to the watcher.
     * 
     * @param entry The added bean entry
     * @param watcher The watching object
     */
    void add( BeanEntry<Q, T> entry, W watcher )
        throws Exception;

    /**
     * Processes the removed {@link BeanEntry} and sends the necessary updates to the watcher.
     * 
     * @param entry The removed bean entry
     * @param watcher The watching object
     */
    void remove( BeanEntry<Q, T> entry, W watcher )
        throws Exception;
}
