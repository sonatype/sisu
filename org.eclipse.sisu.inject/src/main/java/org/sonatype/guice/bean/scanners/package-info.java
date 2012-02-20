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

