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
import com.google.inject.TypeLiteral;

/**
 * Publisher of {@link Binding}s to interested {@link BindingSubscriber}s.
 */
public interface BindingPublisher
{
    /**
     * Subscribes the given {@link BindingSubscriber} to receive {@link Binding}s of the given type.
     * 
     * @param type The binding type
     * @param subscriber The subscriber
     */
    <T> void subscribe( TypeLiteral<T> type, BindingSubscriber subscriber );

    /**
     * Determines whether or not the given {@link Binding} belongs to this publisher.
     * 
     * @param binding The binding
     * @return {@code true} if the binding belongs to this publisher; otherwise {@code false}
     */
    <T> boolean contains( Binding<T> binding );

    /**
     * Stops the given {@link BindingSubscriber} from receiving {@link Binding}s of the given type.
     * 
     * @param type The binding type
     * @param subscriber The subscriber
     */
    <T> void unsubscribe( TypeLiteral<T> type, BindingSubscriber subscriber );
}
