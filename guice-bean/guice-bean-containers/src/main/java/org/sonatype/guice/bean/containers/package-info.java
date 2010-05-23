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
 * Bean containers.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.containers.Main}
 * <dd>Classic main entry point that creates a static {@link com.google.inject.Injector} for the current class-path.
 * <dt>{@link org.sonatype.guice.bean.containers.Activator}
 * <dd>OSGi {@link org.osgi.framework.BundleActivator} that maintains a dynamic injector graph as bundles come and go.
 * <dt>{@link org.sonatype.guice.bean.containers.InjectedTestCase}
 * <dd>JUnit {@link junit.framework.TestCase} that automatically binds and injects itself.
 * </dl>
 */
package org.sonatype.guice.bean.containers;