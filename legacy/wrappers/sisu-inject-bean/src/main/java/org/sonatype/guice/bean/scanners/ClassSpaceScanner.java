/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.guice.bean.scanners;

import java.net.URL;

import org.eclipse.sisu.inject.Legacy;
import org.eclipse.sisu.space.asm.Opcodes;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;

@Deprecated
public final class ClassSpaceScanner
{
    static final Legacy<org.eclipse.sisu.space.ClassSpace> LEGACY_CLASS_SPACE =
        Legacy.<org.eclipse.sisu.space.ClassSpace, ClassSpace> as( ClassSpace.class );

    private final org.eclipse.sisu.space.SpaceScanner delegate;

    public ClassSpaceScanner( final ClassSpace space )
    {
        delegate = new org.eclipse.sisu.space.SpaceScanner( space );
    }

    public void accept( final ClassSpaceVisitor visitor )
    {
        delegate.accept( adapt( visitor ) );
    }

    public static void accept( final ClassVisitor visitor, final URL url )
    {
        org.eclipse.sisu.space.SpaceScanner.accept( adapt( visitor ), url );
    }

    public static boolean verify( final ClassSpace space, final Class<?>... specification )
    {
        return org.eclipse.sisu.space.QualifiedTypeVisitor.verify( space, specification );
    }

    public static org.eclipse.sisu.space.SpaceVisitor adapt( final ClassSpaceVisitor delegate )
    {
        return null == delegate ? null : new org.eclipse.sisu.space.SpaceVisitor()
        {
            public void enterSpace( final org.eclipse.sisu.space.ClassSpace space )
            {
                delegate.visit( (ClassSpace) LEGACY_CLASS_SPACE.proxy( space ) );
            }

            public org.eclipse.sisu.space.ClassVisitor visitClass( final URL url )
            {
                return adapt( delegate.visitClass( url ) );
            }

            public void leaveSpace()
            {
                delegate.visitEnd();
            }
        };
    }

    static org.eclipse.sisu.space.ClassVisitor adapt( final ClassVisitor delegate )
    {
        return null == delegate ? null : new org.eclipse.sisu.space.ClassVisitor()
        {
            public void enterClass( final int modifiers, final String name, final String _extends,
                                    final String[] _implements )
            {
                delegate.visit( Opcodes.V1_5, modifiers, name, null, _extends, _implements );
            }

            public org.eclipse.sisu.space.AnnotationVisitor visitAnnotation( final String desc )
            {
                return adapt( delegate.visitAnnotation( desc, true ) );
            }

            public void leaveClass()
            {
                delegate.visitEnd();
            }
        };
    }

    static org.eclipse.sisu.space.AnnotationVisitor adapt( final AnnotationVisitor delegate )
    {
        return null == delegate ? null : new org.eclipse.sisu.space.AnnotationVisitor()
        {
            public void enterAnnotation()
            {
                // no-op
            }

            public void visitElement( final String name, final Object value )
            {
                delegate.visit( name, value );
            }

            public void leaveAnnotation()
            {
                delegate.visitEnd();
            }
        };
    }
}
