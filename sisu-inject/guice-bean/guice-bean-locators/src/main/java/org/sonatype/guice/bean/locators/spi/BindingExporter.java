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

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Exports {@link Binding}s of various types.
 */
public interface BindingExporter
{
    /**
     * Publishes {@link Binding}s of the requested type to the given {@link BindingImporter}.<br>
     * The {@link BindingExporter} is allowed to publish bindings asynchronously if it wants.
     * 
     * @param type The binding type
     * @param importer The importer
     */
    <T> void publish( TypeLiteral<T> type, BindingImporter importer );

    /**
     * Removes {@link Binding}s of the requested type from the given {@link BindingImporter}.<br>
     * The {@link BindingExporter} is allowed to remove bindings asynchronously if it wants.
     * 
     * @param type The binding type
     * @param importer The importer
     */
    <T> void remove( TypeLiteral<T> type, BindingImporter importer );
}
