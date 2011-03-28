/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.scanners;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import junit.framework.TestCase;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;
import org.sonatype.guice.bean.scanners.asm.Type;
import org.sonatype.guice.bean.scanners.barf.Handler;
import org.sonatype.guice.bean.scanners.index.SisuIndexFinder;

public class QualifiedScanningTest
    extends TestCase
{
    @Named
    interface A
    {
    }

    @Named
    static abstract class B
    {
    }

    @Named
    static class C
    {
    }

    @Qualifier
    @Retention( RetentionPolicy.RUNTIME )
    public @interface Legacy
    {
    }

    @Named
    @Legacy
    static class D
    {
    }

    @Legacy
    @Named
    static class E
    {
    }

    static class F
        extends B
    {
    }

    @Singleton
    static class G
    {
    }

    static class TestListener
        implements QualifiedTypeListener
    {
        final List<String> ids = new ArrayList<String>();

        final Set<Object> sources = new HashSet<Object>();

        public void hear( final Annotation qualifier, final Class<?> clazz, final Object source )
        {
            ids.add( qualifier + ":" + clazz );
            sources.add( source );
        }
    }

    public void testQualifiedScanning()
    {
        final TestListener listener = new TestListener();
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        new ClassSpaceScanner( space ).accept( new QualifiedTypeVisitor( listener ) );
        assertEquals( 5, listener.ids.size() );

        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + C.class ) );
        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + D.class ) );
        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + E.class ) );

        assertTrue( listener.ids.contains( "@" + Legacy.class.getName() + "():" + D.class ) );
        assertTrue( listener.ids.contains( "@" + Legacy.class.getName() + "():" + E.class ) );
    }

    public void testAdaptedScanning()
    {
        final TestListener listener = new TestListener();
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        final ClassSpaceVisitor visitor = new QualifiedTypeVisitor( listener );
        new ClassSpaceScanner( space ).accept( new ClassSpaceVisitor()
        {
            public void visit( final ClassSpace _space )
            {
                visitor.visit( _space );
            }

            public ClassVisitor visitClass( final URL url )
            {
                if ( url.toString().contains( "$D.class" ) )
                {
                    return null;
                }
                return visitor.visitClass( url );
            }

            public void visitEnd()
            {
                visitor.visitEnd();
            }
        } );

        assertEquals( 3, listener.ids.size() );

        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + C.class ) );
        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + E.class ) );
        assertTrue( listener.ids.contains( "@" + Legacy.class.getName() + "():" + E.class ) );
    }

    public void testFilteredScanning()
    {
        final TestListener listener = new TestListener();
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        final ClassSpaceVisitor visitor = new QualifiedTypeVisitor( listener );
        new ClassSpaceScanner( new ClassFinder()
        {
            public Enumeration<URL> findClasses( final ClassSpace space2 )
            {
                return space2.findEntries( null, "*D.class", true );
            }
        }, space ).accept( visitor );

        assertEquals( 2, listener.ids.size() );

        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + D.class ) );
        assertTrue( listener.ids.contains( "@" + Legacy.class.getName() + "():" + D.class ) );
    }

    public void testIndexedScanning()
    {
        final TestListener listener = new TestListener();
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        final ClassSpaceVisitor visitor = new QualifiedTypeVisitor( listener );
        new ClassSpaceScanner( new SisuIndexFinder( false ), space ).accept( visitor );

        // we deliberately use a partial index

        assertEquals( 3, listener.ids.size() );

        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + C.class ) );
        assertTrue( listener.ids.contains( "@" + Named.class.getName() + "(value=):" + D.class ) );
        assertTrue( listener.ids.contains( "@" + Legacy.class.getName() + "():" + D.class ) );
    }

    public void testBrokenScanning()
        throws IOException
    {
        // System.setProperty( "java.protocol.handler.pkgs", getClass().getPackage().getName() );
        URL.setURLStreamHandlerFactory( new URLStreamHandlerFactory()
        {
            public URLStreamHandler createURLStreamHandler( final String protocol )
            {
                if ( "barf".equals( protocol ) )
                {
                    return new Handler();
                }
                return null;
            }
        } );

        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );

        final URL badURL = new URL( "barf:up/" );
        final ClassSpace brokenResourceSpace = new ClassSpace()
        {
            public Class<?> loadClass( final String name )
            {
                return space.loadClass( name );
            }

            public DeferredClass<?> deferLoadClass( final String name )
            {
                return space.deferLoadClass( name );
            }

            public Enumeration<URL> getResources( final String name )
            {
                return space.getResources( name );
            }

            public URL getResource( final String name )
            {
                return badURL;
            }

            public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
            {
                return Collections.enumeration( Collections.singleton( badURL ) );
            }

            public boolean loadedClass( final Class<?> clazz )
            {
                return false;
            }
        };

        new ClassSpaceScanner( brokenResourceSpace ).accept( new QualifiedTypeVisitor( null ) );

        final ClassSpace brokenLoadSpace = new ClassSpace()
        {
            public Class<?> loadClass( final String name )
            {
                throw new TypeNotPresentException( name, new ClassNotFoundException( name ) );
            }

            public DeferredClass<?> deferLoadClass( final String name )
            {
                return space.deferLoadClass( name );
            }

            public Enumeration<URL> getResources( final String name )
            {
                return space.getResources( name );
            }

            public URL getResource( final String name )
            {
                return space.getResource( name );
            }

            public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
            {
                return space.findEntries( path, glob, recurse );
            }

            public boolean loadedClass( final Class<?> clazz )
            {
                return false;
            }
        };

        new ClassSpaceScanner( brokenLoadSpace ).accept( new QualifiedTypeVisitor( null ) );

        ClassSpaceScanner.accept( null, null );

        assertFalse( new SisuIndexFinder( false ).findClasses( brokenResourceSpace ).hasMoreElements() );
    }

    public void testSourceDetection()
        throws MalformedURLException
    {
        final TestListener listener = new TestListener();

        final QualifiedTypeVisitor visitor = new QualifiedTypeVisitor( listener );

        visitor.visit( new URLClassSpace( getClass().getClassLoader() ) );

        assertEquals( 0, listener.sources.size() );

        visitor.visitClass( new URL( "file:target/classes/java/lang/Object.class" ) );
        visitor.visit( 0, 0, Type.getInternalName( Object.class ), null, null, null );
        visitor.visitAnnotation( Type.getDescriptor( Named.class ), true );

        assertEquals( 1, listener.sources.size() );
        assertTrue( listener.sources.contains( "target/classes/" ) );

        visitor.visitClass( new URL( "jar:file:bar.jar!/java/lang/String.class" ) );
        visitor.visit( 0, 0, Type.getInternalName( String.class ), null, null, null );
        visitor.visitAnnotation( Type.getDescriptor( Named.class ), true );

        assertEquals( 2, listener.sources.size() );
        assertTrue( listener.sources.contains( "target/classes/" ) );
        assertTrue( listener.sources.contains( "file:bar.jar!/" ) );

        visitor.visitClass( new URL( "file:some/obfuscated/location" ) );
        visitor.visit( 0, 0, Type.getInternalName( Integer.class ), null, null, null );
        visitor.visitAnnotation( Type.getDescriptor( Named.class ), true );

        assertEquals( 3, listener.sources.size() );
        assertTrue( listener.sources.contains( "target/classes/" ) );
        assertTrue( listener.sources.contains( "file:bar.jar!/" ) );
        assertTrue( listener.sources.contains( "some/obfuscated/location" ) );
    }

    public void testOptionalLogging()
        throws Exception
    {
        final Level level = Logger.getLogger( "" ).getLevel();
        try
        {
            Logger.getLogger( "" ).setLevel( Level.SEVERE );

            // check everything still works without any SLF4J jars
            final ClassLoader noLoggingLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "slf4j" ) )
                        {
                            throw new ClassNotFoundException( name );
                        }
                        if ( name.contains( "cobertura" ) )
                        {
                            return QualifiedScanningTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noLoggingLoader.loadClass( BrokenScanningExample.class.getName() ).newInstance();
        }
        finally
        {
            Logger.getLogger( "" ).setLevel( level );
        }
    }

    public void testEmptyMethods()
    {
        final AnnotationVisitor emptyAnnotationVisitor = new EmptyAnnotationVisitor();

        emptyAnnotationVisitor.visit( null, null );
        emptyAnnotationVisitor.visitEnum( null, null, null );
        emptyAnnotationVisitor.visitAnnotation( null, null );
        emptyAnnotationVisitor.visitArray( null );
        emptyAnnotationVisitor.visitEnd();

        final ClassVisitor emptyClassVisitor = new EmptyClassVisitor();

        emptyClassVisitor.visit( 0, 0, null, null, null, null );
        emptyClassVisitor.visitAnnotation( null, false );
        emptyClassVisitor.visitAttribute( null );
        emptyClassVisitor.visitSource( null, null );
        emptyClassVisitor.visitEnd();
    }
}
