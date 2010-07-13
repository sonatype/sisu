/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.config;

import java.util.Map.Entry;

import org.sonatype.guice.bean.reflect.DeferredClass;

/**
 * Plexus bean mapping; from hint->instance.
 */
public interface PlexusBean<T>
    extends Entry<String, T>
{
    /**
     * @return Human readable bean description
     */
    String getDescription();

    /**
     * @return Deferred implementation class
     */
    DeferredClass<T> getImplementationClass();
}