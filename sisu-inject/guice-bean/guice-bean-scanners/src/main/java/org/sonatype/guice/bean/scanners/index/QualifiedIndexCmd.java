/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.scanners.index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.guice.bean.scanners.QualifiedTypeVisitor;

public final class QualifiedIndexCmd
    extends AbstractQualifiedIndex
    implements QualifiedTypeListener
{
    public static void main( final String[] args )
    {
        final QualifiedIndexCmd indexer = new QualifiedIndexCmd();
        final ClassSpace space = new URLClassSpace( QualifiedIndexCmd.class.getClassLoader() );
        new ClassSpaceScanner( space ).accept( new QualifiedTypeVisitor( indexer ) );
        indexer.saveIndex();
    }

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        updateIndex( qualifier.annotationType().getName(), qualifiedType.getName() );
    }

    @Override
    protected void info( final String message )
    {
        System.out.println( "[INFO] " + message );
    }

    @Override
    protected void warn( final String message )
    {
        System.out.println( "[WARN] " + message );
    }

    @Override
    protected Writer getWriter( final String path )
        throws IOException
    {
        final File index = new File( path );
        index.getParentFile().mkdirs();
        return new FileWriter( index );
    }
}
