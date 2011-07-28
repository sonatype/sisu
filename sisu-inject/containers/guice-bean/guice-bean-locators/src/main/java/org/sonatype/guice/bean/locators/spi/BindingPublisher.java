/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
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
     * @return {@code true} if the publisher may send bindings of this type; otherwise {@code false}
     */
    <T> boolean subscribe( TypeLiteral<T> type, BindingSubscriber subscriber );

    /**
     * Determines whether or not the given {@link Binding} reference belongs to this publisher.
     * 
     * @param binding The binding
     * @return {@code true} if the binding belongs to this publisher; otherwise {@code false}
     */
    <T> boolean containsThis( Binding<T> binding );

    /**
     * Stops the given {@link BindingSubscriber} from receiving {@link Binding}s of the given type.
     * 
     * @param type The binding type
     * @param subscriber The subscriber
     */
    <T> void unsubscribe( TypeLiteral<T> type, BindingSubscriber subscriber );
}
