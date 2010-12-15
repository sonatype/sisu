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
package org.sonatype.guice.bean.locators;

import org.sonatype.guice.bean.locators.spi.BindingExporter;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;

/**
 * Mutable {@link BeanLocator} that searches bindings from zero or more {@link BindingExporter}s.
 */
@ImplementedBy( DefaultBeanLocator.class )
public interface MutableBeanLocator
    extends BeanLocator
{
    /**
     * Adds qualified beans exported by the given ranked {@link BindingExporter} to any exposed/watched collections.
     * 
     * @param exporter The new exporter
     * @param rank The assigned rank
     */
    void add( BindingExporter exporter, int rank );

    /**
     * Removes qualified beans exported by the given {@link BindingExporter} from any exposed/watched collections.
     * 
     * @param exporter The old exporter
     */
    void remove( BindingExporter exporter );

    /**
     * Removes all qualified beans from any exposed/watched collections.
     */
    void clear();

    @Deprecated
    void add( Injector injector );

    @Deprecated
    void remove( Injector injector );
}
