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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.guice.bean.scanners.QualifiedTypeVisitor;

/**
 * Command-line utility that can generate {@code META-INF/sisu} index files for a space-separated list of JARs.
 */
public final class QualifiedIndexCmd
    extends AbstractSisuIndex
    implements QualifiedTypeListener
{
    // ----------------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------------

    public static void main( final String[] args )
        throws MalformedURLException
    {
        final URL[] urls = new URL[args.length];
        for ( int i = 0; i < args.length; i++ )
        {
            urls[i] = new File( args[i] ).toURI().toURL();
        }

        final QualifiedIndexCmd indexer = new QualifiedIndexCmd();
        try
        {
            final ClassLoader parent = QualifiedIndexCmd.class.getClassLoader();
            final ClassLoader loader = urls.length > 0 ? URLClassLoader.newInstance( urls, parent ) : parent;
            new ClassSpaceScanner( new URLClassSpace( loader ) ).accept( new QualifiedTypeVisitor( indexer ) );
        }
        finally
        {
            indexer.saveIndex();
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        addIndexEntry( qualifier.annotationType().getName(), qualifiedType.getName() );
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

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
        if ( index.getParentFile().mkdirs() )
        {
            return new FileWriter( index );
        }
        throw new IOException( "Error creating: " + index );
    }
}
