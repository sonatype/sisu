/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.locators.spi;

import java.util.List;

import org.sonatype.guice.bean.locators.MutableBeanLocator;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Contributes {@link Binding}s to the {@link MutableBeanLocator}.
 */
public interface Bindings
    extends Rankable
{
    /**
     * Returns {@link Binding}s involving the given interface type.
     * 
     * @param type The interface type
     * @return The interface bindings
     */
    <T> List<Binding<T>> forType( TypeLiteral<T> type );
}
