/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
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

