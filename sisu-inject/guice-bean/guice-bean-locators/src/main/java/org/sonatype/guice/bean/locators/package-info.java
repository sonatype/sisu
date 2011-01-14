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

/**
 * Locate qualified bean implementations across multiple injectors.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.locators.BeanLocator}
 * <dd>Finds and tracks bean implementations annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link org.sonatype.guice.bean.locators.MutableBeanLocator}
 * <dd>Mutable {@link org.sonatype.guice.bean.locators.BeanLocator} that distributes bindings from zero or more {@link org.sonatype.guice.bean.locators.spi.BindingPublisher}s.
 * <dt>{@link org.sonatype.guice.bean.locators.BeanDescription}
 * <dd>Source location mixin used to supply descriptions to the {@link org.sonatype.guice.bean.locators.BeanLocator}.
 * <dt>{@link org.sonatype.guice.bean.locators.HiddenBinding}
 * <dd>Source location mixin used to hide bindings from the {@link org.sonatype.guice.bean.locators.BeanLocator}.
 * </dl>
 */
package org.sonatype.guice.bean.locators;
