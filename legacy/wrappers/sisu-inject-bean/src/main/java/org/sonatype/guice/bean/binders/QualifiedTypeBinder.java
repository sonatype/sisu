/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.guice.bean.binders;

import java.lang.annotation.Annotation;

import org.sonatype.guice.bean.scanners.QualifiedTypeListener;

import com.google.inject.Binder;

@Deprecated
public final class QualifiedTypeBinder
    implements QualifiedTypeListener
{
    private final org.eclipse.sisu.space.QualifiedTypeBinder delegate;

    public QualifiedTypeBinder( final Binder binder )
    {
        delegate = new org.eclipse.sisu.space.QualifiedTypeBinder( binder );
    }

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        delegate.hear( qualifiedType, source );
    }
}
