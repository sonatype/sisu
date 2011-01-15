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
 * <a href="http://asm.ow2.org/">ASM</a>-based bean scanning.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.scanners.ClassSpaceVisitor}
 * <dd>ASM-style visitor that can visit a {@link org.sonatype.guice.bean.reflect.ClassSpace}.
 * <dt>{@link org.sonatype.guice.bean.scanners.ClassSpaceScanner}
 * <dd>Makes a {@link org.sonatype.guice.bean.scanners.ClassSpaceVisitor} visit a {@link org.sonatype.guice.bean.reflect.ClassSpace}.
 * <dt>{@link org.sonatype.guice.bean.scanners.QualifiedTypeListener}
 * <dd>Listens out for types annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link org.sonatype.guice.bean.scanners.QualifiedTypeVisitor}
 * <dd>{@link org.sonatype.guice.bean.scanners.ClassSpaceVisitor} that reports types with {@link javax.inject.Qualifier} annotations.
 * </dl>
 */
package org.sonatype.guice.bean.scanners;

