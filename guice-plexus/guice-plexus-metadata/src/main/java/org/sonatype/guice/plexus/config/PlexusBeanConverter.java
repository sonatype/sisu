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

import com.google.inject.TypeLiteral;

/**
 * Service that converts values into various beans by following Plexus configuration rules.
 */
public interface PlexusBeanConverter
{
    /**
     * Converts the given constant value to a bean of the given type.
     * 
     * @param type The expected bean type
     * @param value The constant value
     * @return Bean of the given type, based on the given constant value
     */
    <T> T convert( TypeLiteral<T> type, String value );
}