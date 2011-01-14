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
package org.sonatype.guice.bean.inject;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Provides custom {@link PropertyBinder}s for beans that contain one or more properties.
 */
public interface BeanBinder
{
    /**
     * Returns the appropriate {@link PropertyBinder} for the given bean type.
     * 
     * @param type The bean type
     * @param encounter The Guice type encounter
     * @return Property binder for the given type; {@code null} if no binder is applicable
     */
    <B> PropertyBinder bindBean( TypeLiteral<B> type, TypeEncounter<B> encounter );
}
