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
package org.sonatype.guice.plexus.scanners;

import java.lang.annotation.Annotation;
import java.net.URL;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.Down;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;
import org.sonatype.guice.bean.scanners.asm.Type;

@Deprecated
@SuppressWarnings( "unchecked" )
public final class PlexusTypeVisitor
    implements ClassSpaceVisitor, ClassVisitor
{
    private final org.eclipse.sisu.plexus.PlexusTypeVisitor delegate;

    private ClassVisitor visitor;

    public PlexusTypeVisitor( final PlexusTypeListener listener )
    {
        delegate = new org.eclipse.sisu.plexus.PlexusTypeVisitor( adapt( listener ) );
    }

    public void visit( final ClassSpace space )
    {
        delegate.enter( space );
    }

    public ClassVisitor visitClass( final URL url )
    {
        visitor = adapt( delegate.visitClass( url ) );
        return this;
    }

    public void visit( int version, int access, String name, String signature, String superName, String[] interfaces )
    {
        if ( null != visitor )
        {
            visitor.visit( version, access, name, signature, superName, interfaces );
        }
    }

    public AnnotationVisitor visitAnnotation( String desc, boolean visible )
    {
        return null != visitor ? visitor.visitAnnotation( desc, visible ) : null;
    }

    public void visitEnd()
    {
        if ( null != visitor )
        {
            visitor.visitEnd();
            visitor = null;
        }
        else
        {
            delegate.leave();
        }
    }

    private static org.eclipse.sisu.plexus.PlexusTypeListener adapt( final PlexusTypeListener delegate )
    {
        return new org.eclipse.sisu.plexus.PlexusTypeListener()
        {
            public void hear( @Deprecated final Annotation qualifier, final Class<?> qualifiedType, final Object source )
            {
                delegate.hear( qualifier, qualifiedType, source );
            }

            public void hear( final Component component, final org.eclipse.sisu.inject.DeferredClass<?> implementation,
                              final Object source )
            {
                delegate.hear( component, Down.cast( DeferredClass.class, implementation ), source );
            }
        };
    }

    static ClassVisitor adapt( final org.eclipse.sisu.space.ClassVisitor delegate )
    {
        return null == delegate ? null : new ClassVisitor()
        {
            public void visit( final int version, final int access, final String name, final String signature,
                               final String superName, final String[] interfaces )
            {
                delegate.enter( access, name, superName, interfaces );
            }

            public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
            {
                return adapt( delegate.visitAnnotation( desc ) );
            }

            public void visitEnd()
            {
                delegate.leave();
            }
        };
    }

    static AnnotationVisitor adapt( final org.eclipse.sisu.space.AnnotationVisitor delegate )
    {
        return null == delegate ? null : new AnnotationVisitor()
        {
            {
                delegate.enter();
            }

            public void visit( final String name, final Object value )
            {
                delegate.visitElement( name, value instanceof Type ? ( (Type) value ).getClassName() : value );
            }

            public void visitEnd()
            {
                delegate.leave();
            }
        };
    }
}
