/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.locators.spi;

import com.google.inject.Binding;

/**
 * Publisher of {@link Binding}s to interested {@link BindingSubscriber}s.
 */
public interface BindingPublisher
{
    /**
     * Subscribes the given {@link BindingSubscriber} to receive {@link Binding}s.
     * 
     * @param subscriber The subscriber
     */
    <T> void subscribe( BindingSubscriber<T> subscriber );

    /**
     * Stops the given {@link BindingSubscriber} from receiving {@link Binding}s.
     * 
     * @param subscriber The subscriber
     */
    <T> void unsubscribe( BindingSubscriber<T> subscriber );
}
