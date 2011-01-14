/**
 * Copyright (c) 2009-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.reflect;

/**
 * Simple {@link Class} reference that supports deferred loading.
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
