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
import java.util.Enumeration;

import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.space.asm.Opcodes;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;

@Deprecated
public final class ClassSpaceScanner
{
    private final org.eclipse.sisu.space.ClassSpaceScanner delegate;

    public ClassSpaceScanner( final ClassSpace space )
    {
        delegate = new org.eclipse.sisu.space.ClassSpaceScanner( space );
    }

    public void accept( final ClassSpaceVisitor visitor )
    {
        delegate.accept( adapt( visitor ) );
    }

    public static void accept( final ClassVisitor visitor, final URL url )
    {
        org.eclipse.sisu.space.ClassSpaceScanner.accept( adapt( visitor ), url );
    }

    public static boolean verify( final ClassSpace space, final Class<?>... specification )
    {
        return org.eclipse.sisu.space.QualifiedTypeVisitor.verify( space, specification );
    }

    public static org.eclipse.sisu.space.ClassSpaceVisitor adapt( final ClassSpaceVisitor delegate )
    {
        return new org.eclipse.sisu.space.ClassSpaceVisitor()
        {
            public void enter( org.eclipse.sisu.space.ClassSpace space )
            {
                delegate.visit( adapt( space ) );
            }

            public org.eclipse.sisu.space.ClassVisitor visitClass( URL url )
            {
                return adapt( delegate.visitClass( url ) );
            }

            public void leave()
            {
                delegate.visitEnd();
            }
        };
    }

    static org.eclipse.sisu.space.ClassVisitor adapt( final ClassVisitor delegate )
    {
        return null == delegate ? null : new org.eclipse.sisu.space.ClassVisitor()
        {
            public void enter( int modifiers, String name, String _extends, String[] _implements )
            {
                delegate.visit( Opcodes.V1_5, modifiers, name, null, _extends, _implements );
            }

            public org.eclipse.sisu.space.AnnotationVisitor visitAnnotation( String desc )
            {
                return adapt( delegate.visitAnnotation( desc, true ) );
            }

            public void leave()
            {
                delegate.visitEnd();
            }
        };
    }

    static org.eclipse.sisu.space.AnnotationVisitor adapt( final AnnotationVisitor delegate )
    {
        return null == delegate ? null : new org.eclipse.sisu.space.AnnotationVisitor()
        {
            public void enter()
            {
                // no-op
            }

            public void visitElement( String name, Object value )
            {
                delegate.visit( name, value );
            }

            public void leave()
            {
                delegate.visitEnd();
            }
        };
    }

    static ClassSpace adapt( final org.eclipse.sisu.space.ClassSpace delegate )
    {
        return new ClassSpace()
        {
            public Class<?> loadClass( String name )
                throws TypeNotPresentException
            {
                return delegate.loadClass( name );
            }

            public DeferredClass<?> deferLoadClass( String name )
            {
                return delegate.deferLoadClass( name );
            }

            public URL getResource( String name )
            {
                return delegate.getResource( name );
            }

            public Enumeration<URL> getResources( String name )
            {
                return delegate.getResources( name );
            }

            public Enumeration<URL> findEntries( String path, String glob, boolean recurse )
            {
                return delegate.findEntries( path, glob, recurse );
            }
        };
    }
}
