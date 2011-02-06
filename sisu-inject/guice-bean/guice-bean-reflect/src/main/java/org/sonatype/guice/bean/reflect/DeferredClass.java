/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

/**
 * Placeholder {@link Class}; postpones classloading until absolutely necessary.
 */
public interface DeferredClass<T>
{
    /**
     * Retrieves the class, for example from a cache or a class loader.
     * 
     * @return Class instance
     */
    Class<T> load()
        throws TypeNotPresentException;

    /**
     * Returns the name of the deferred class.
     * 
     * @return Class name
     */
    String getName();

    /**
     * Returns a provider based on the deferred class.
     * 
     * @return Deferred provider
     */
    DeferredProvider<T> asProvider();
}
