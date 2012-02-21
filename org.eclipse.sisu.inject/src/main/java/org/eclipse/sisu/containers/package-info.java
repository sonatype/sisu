/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
/**
 * Bean containers.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.eclipse.sisu.containers.Main}
 * <dd>Classic main entry point that creates a static {@link com.google.inject.Injector} for the current class-path.
 * <dt>{@link org.eclipse.sisu.containers.SisuActivator}
 * <dd>OSGi {@link org.osgi.framework.BundleActivator} that maintains a dynamic injector graph as bundles come and go.
 * <dt>{@link org.eclipse.sisu.containers.InjectedTestCase}
 * <dd>JUnit {@link junit.framework.TestCase} that automatically binds and injects itself.
 * </dl>
 */
package org.eclipse.sisu.containers;

