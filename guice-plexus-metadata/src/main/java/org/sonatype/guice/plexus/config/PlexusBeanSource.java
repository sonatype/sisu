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

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.DeferredClass;

/**
 * Source of Plexus component beans and associated metadata.
 */
public interface PlexusBeanSource
{
    /**
     * Finds Plexus component beans by scanning, lookup, or other such methods.
     * 
     * @return Map of Plexus component beans
     */
    Map<Component, DeferredClass<?>> findPlexusComponentBeans();

    /**
     * Returns metadata associated with the given Plexus bean implementation.
     * 
     * @param implementation The bean implementation
     * @return Metadata associated with the given bean
     */
    PlexusBeanMetadata getBeanMetadata( Class<?> implementation );
}
