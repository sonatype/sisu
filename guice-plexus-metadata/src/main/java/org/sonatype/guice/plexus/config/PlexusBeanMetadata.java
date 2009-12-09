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

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;

/**
 * Supplies metadata associated with a particular Plexus bean implementation.
 */
public interface PlexusBeanMetadata
{
    /**
     * @return {@code true} if there is no more metadata; otherwise {@code false}
     */
    boolean isEmpty();

    /**
     * Returns @{@link Configuration} metadata for the given property of the Plexus bean.
     * 
     * @param property The bean property
     * @return Configuration metadata; {@code null} if no such metadata is available
     */
    Configuration getConfiguration( BeanProperty<?> property );

    /**
     * Returns @{@link Requirement} metadata for the given property of the Plexus bean.
     * 
     * @param property The bean property
     * @return Requirement metadata; {@code null} if no such metadata is available
     */
    Requirement getRequirement( BeanProperty<?> property );
}
