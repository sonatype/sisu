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
