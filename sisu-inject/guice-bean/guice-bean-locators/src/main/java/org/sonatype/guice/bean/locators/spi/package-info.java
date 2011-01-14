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
 * SPI for contributing {@link com.google.inject.Binding}s to the {@link org.sonatype.guice.bean.locators.MutableBeanLocator}.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.locators.spi.BindingDistributor}
 * <dd>Distributor of {@link com.google.inject.Binding}s retrieved from a series of {@link org.sonatype.guice.bean.locators.spi.BindingPublisher}s.
 * <dt>{@link org.sonatype.guice.bean.locators.spi.BindingPublisher}
 * <dd>Publisher of {@link com.google.inject.Binding}s to interested {@link org.sonatype.guice.bean.locators.spi.BindingSubscriber}s.
 * <dt>{@link org.sonatype.guice.bean.locators.spi.BindingSubscriber}
 * <dd>Subscriber of {@link com.google.inject.Binding}s from one or more {@link org.sonatype.guice.bean.locators.spi.BindingPublisher}s.
 * </dl>
 */
package org.sonatype.guice.bean.locators.spi;

