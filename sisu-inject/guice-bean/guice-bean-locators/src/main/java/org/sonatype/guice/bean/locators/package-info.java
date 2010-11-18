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
 * <dt>{@link org.sonatype.guice.bean.locators.QualifiedBean}
 * <dd>Qualified bean {@link java.util.Map.Entry} and {@link javax.inject.Provider}.
 * <dt>{@link org.sonatype.guice.bean.locators.BeanDescription}
 * <dd>Mixin interface used to supply descriptions to the {@link org.sonatype.guice.bean.locators.BeanLocator}.
 * <dt>{@link org.sonatype.guice.bean.locators.HiddenSource}
 * <dd>Marker interface used to hide bindings from the {@link org.sonatype.guice.bean.locators.BeanLocator}.
 * </dl>
 */
package org.sonatype.guice.bean.locators;