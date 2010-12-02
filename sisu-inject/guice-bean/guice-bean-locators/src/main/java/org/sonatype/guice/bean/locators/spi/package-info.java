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
 * SPI for contributing {@link com.google.inject.Binding}s to the {@link org.sonatype.guice.bean.locators.MutableBeanLocator}.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.locators.spi.Bindings}
 * <dd>Contributes {@link com.google.inject.Binding}s.
 * <dt>{@link org.sonatype.guice.bean.locators.spi.BindingHub}
 * <dd>Distributes {@link com.google.inject.Binding}s to {@link org.sonatype.guice.bean.locators.spi.BindingSpoke}s.
 * <dt>{@link org.sonatype.guice.bean.locators.spi.BindingSpoke}
 * <dd>Observes {@link com.google.inject.Binding}s.
 * </dl>
 */
package org.sonatype.guice.bean.locators.spi;