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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.guice.bean.scanners.QualifiedTypeVisitor;

/**
 * Command-line utility that generates a qualified class index for a space-separated list of JARs.
 */
public final class SisuIndex
    extends AbstractSisuIndex
    implements QualifiedTypeListener
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    public static final String NAMED = Named.class.getName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final File targetDirectory;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SisuIndex( final File targetDirectory )
    {
        this.targetDirectory = targetDirectory;
    }

    // ----------------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------------

    public static void main( final String[] args )
    {
        final List<URL> classPath = new ArrayList<URL>( args.length );
        for ( final String path : args )
        {
            try
            {
                classPath.add( new File( path ).toURI().toURL() );
            }
            catch ( final MalformedURLException e )
            {
                Logs.warn( "Bad classpath element: {}", path, e );
            }
        }
        new SisuIndex( new File( "." ) ).index( classPath );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void index( final List<URL> classPath )
    {
        final URL[] urls = classPath.toArray( new URL[classPath.size()] );

        final ClassLoader parent = SisuIndex.class.getClassLoader();
        final ClassLoader loader = urls.length > 0 ? URLClassLoader.newInstance( urls, parent ) : parent;

        try
        {
            new ClassSpaceScanner( new URLClassSpace( loader ) ).accept( new QualifiedTypeVisitor( this ) );
        }
        finally
        {
            flushIndex();
        }
    }

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        addClassToIndex( SisuIndex.NAMED, qualifiedType.getName() );
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
    protected Reader getReader( final String path )
        throws IOException
    {
        return new FileReader( new File( targetDirectory, path ) );
    }

    @Override
    protected Writer getWriter( final String path )
        throws IOException
    {
        final File index = new File( targetDirectory, path );
        final File parent = index.getParentFile();
        if ( parent.isDirectory() || parent.mkdirs() )
        {
            return new FileWriter( index );
        }
        throw new IOException( "Error creating: " + parent );
    }
}
