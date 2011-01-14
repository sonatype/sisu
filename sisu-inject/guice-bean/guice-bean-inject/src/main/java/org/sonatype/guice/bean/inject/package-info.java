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
