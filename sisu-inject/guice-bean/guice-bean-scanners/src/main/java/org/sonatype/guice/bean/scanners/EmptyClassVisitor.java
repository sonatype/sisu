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

/**
 * Skeletal {@link ClassVisitor} that helps minimize the effort required to implement bytecode scanners.
 */
public class EmptyClassVisitor
    implements ClassVisitor
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
    }

    public void visitSource( final String source, final String debug )
    {
    }

    public void visitOuterClass( final String owner, final String name, final String desc )
    {
    }

    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        return null;
    }

    public void visitAttribute( final Attribute attr )
    {
    }

    public void visitInnerClass( final String name, final String outerName, final String innerName, final int access )
    {
    }

    public FieldVisitor visitField( final int access, final String name, final String desc, final String signature,
                                    final Object value )
    {
        return null;
    }

    public MethodVisitor visitMethod( final int access, final String name, final String desc, final String signature,
                                      final String[] exceptions )
    {
        return null;
    }

    public void visitEnd()
    {
    }
}
