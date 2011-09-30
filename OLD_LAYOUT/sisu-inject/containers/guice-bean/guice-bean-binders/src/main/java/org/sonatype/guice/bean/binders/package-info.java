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
 * Automatic bean binding.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.binders.SpaceModule}
 * <dd>Scans a {@link org.sonatype.guice.bean.reflect.ClassSpace} for beans and adds any qualified bindings.
 * <dt>{@link org.sonatype.guice.bean.binders.WireModule}
 * <dd>Adds {@link org.sonatype.guice.bean.locators.BeanLocator} bindings for any non-local bean dependencies.
 * </dl>
 */
package org.sonatype.guice.bean.binders;

