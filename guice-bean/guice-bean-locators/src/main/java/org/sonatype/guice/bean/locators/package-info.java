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

/**
 * Dynamic bean location.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.locators.BeanLocator}
 * <dd>Dynamic locator of beans annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link org.sonatype.guice.bean.locators.MutableBeanLocator}
 * <dd>Mutable {@link BeanLocator} that tracks zero or more {@link com.google.inject.Injector}s.
 * <dt>{@link org.sonatype.guice.bean.locators.DefaultBeanLocator}
 * <dd>Default {@link BeanLocator} that locates qualified beans across a dynamic group of {@link com.google.inject.Injector}s.
 * </dl>
 */
package org.sonatype.guice.bean.locators;