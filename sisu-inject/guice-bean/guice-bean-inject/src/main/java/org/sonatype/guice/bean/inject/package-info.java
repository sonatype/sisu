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
 * Custom bean injection.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.inject.BeanListener}
 * <dd>{@link com.google.inject.spi.TypeListener} that listens for bean types and wires up their properties.
 * <dt>{@link org.sonatype.guice.bean.inject.BeanBinder}
 * <dd>Provides custom {@link org.sonatype.guice.bean.inject.PropertyBinder}s for bean types.
 * <dt>{@link org.sonatype.guice.bean.inject.PropertyBinder}
 * <dd>Provides custom {@link org.sonatype.guice.bean.inject.PropertyBinding}s for bean properties.
 * <dt>{@link org.sonatype.guice.bean.inject.PropertyBinding}
 * <dd>Injects a customized bean property into bean instances.
 * </dl>
 */
package org.sonatype.guice.bean.inject;

