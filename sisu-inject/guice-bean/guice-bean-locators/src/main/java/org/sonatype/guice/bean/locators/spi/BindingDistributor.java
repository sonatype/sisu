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

/**
 * Distributes {@link Binding}s of various types.
 */
public interface BindingDistributor
{
    /**
     * Distributes bindings exported by the given ranked {@link BindingExporter}.
     * 
     * @param exporter The new exporter
     * @param rank The assigned rank
     */
    void add( BindingExporter exporter, int rank );

    /**
     * Withdraws bindings exported by the given {@link BindingExporter}.
     * 
     * @param exporter The old exporter
     */
    void remove( BindingExporter exporter );

    /**
     * Withdraws all distributed {@link Binding}s.
     */
    void clear();
}
