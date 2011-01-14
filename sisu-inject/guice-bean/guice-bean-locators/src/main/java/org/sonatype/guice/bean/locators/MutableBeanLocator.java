/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;

import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;

/**
 * Mutable {@link BeanLocator} that finds and tracks bindings across zero or more {@link BindingPublisher}s.
 */
@ImplementedBy( DefaultBeanLocator.class )
public interface MutableBeanLocator
    extends BeanLocator, BindingDistributor
{
    /**
     * Adds the given ranked {@link Injector} and distributes its {@link Binding}s. Marked as deprecated because most
     * clients should <b>not</b> call this method; any injector that contains a binding to the {@link BeanLocator} is
     * automatically added to that locator as part of the bootstrapping process.
     * 
     * @param injector The new injector
     * @param rank The assigned rank
     */
    @Deprecated
    void add( Injector injector, int rank );

    /**
     * Removes the given {@link Injector} and its {@link Binding}s.
     * 
     * @param injector The old injector
     */
    void remove( Injector injector );
}
