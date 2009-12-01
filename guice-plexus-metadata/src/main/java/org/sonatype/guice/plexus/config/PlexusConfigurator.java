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

import com.google.inject.TypeLiteral;

/**
 * Configures instances of various value-types for Plexus component beans.
 */
public interface PlexusConfigurator
{
    /**
     * Returns an instance of the given type using the given configuration.
     * 
     * @param configuration The configuration
     * @param asType The expected type
     * @return Instance of the given type, configured accordingly
     */
    <T> T configure( Configuration configuration, TypeLiteral<T> asType );
}