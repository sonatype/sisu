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
 * <a href="http://asm.ow2.org/">ASM</a>-based bean scanning.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.eclipse.sisu.scanners.ClassSpaceVisitor}
 * <dd>ASM-style visitor that can visit a {@link org.eclipse.sisu.reflect.ClassSpace}.
 * <dt>{@link org.eclipse.sisu.scanners.ClassSpaceScanner}
 * <dd>Makes a {@link org.eclipse.sisu.scanners.ClassSpaceVisitor} visit a {@link org.eclipse.sisu.reflect.ClassSpace}.
 * <dt>{@link org.eclipse.sisu.scanners.QualifiedTypeListener}
 * <dd>Listens out for types annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link org.eclipse.sisu.scanners.QualifiedTypeVisitor}
 * <dd>{@link org.eclipse.sisu.scanners.ClassSpaceVisitor} that reports types with {@link javax.inject.Qualifier} annotations.
 * </dl>
 */
package org.eclipse.sisu.scanners;

