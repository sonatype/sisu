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
package org.sonatype.guice.bean.scanners;

import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;

@Deprecated
public class EmptyAnnotationVisitor
    implements AnnotationVisitor
{
    public void visit( final String name, final Object value )
    {
    }

    public void visitEnd()
    {
    }
}
