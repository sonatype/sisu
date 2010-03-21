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
package org.sonatype.guice.bean.scanners;

import org.objectweb.asm.AnnotationVisitor;

/**
 * Skeletal {@link AnnotationVisitor} that helps minimize the effort required to implement bytecode scanners.
 */
class EmptyAnnotationVisitor
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