/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.scanners;

import org.objectweb.asm.AnnotationVisitor;

/**
 * Skeletal {@link AnnotationVisitor} that helps minimize the effort required to implement bytecode scanners.
 */
public class EmptyAnnotationVisitor
    implements AnnotationVisitor
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final String name, final Object value )
    {
    }

    public void visitEnum( final String name, final String desc, final String value )
    {
    }

    public AnnotationVisitor visitAnnotation( final String name, final String desc )
    {
        return null;
    }

    public AnnotationVisitor visitArray( final String name )
    {
        return null;
    }

    public void visitEnd()
    {
    }
}
