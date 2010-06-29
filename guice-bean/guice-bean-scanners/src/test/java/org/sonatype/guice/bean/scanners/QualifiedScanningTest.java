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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import junit.framework.TestCase;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;

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

        public void hear( final Annotation qualifier, final Class<?> clazz )
        {
            ids.add( qualifier + ":" + clazz );
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

    public void testFilteredScanning()
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

    public void testBrokenScanning()
        throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", getClass().getPackage().getName() );
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
                return space.findEntries( path, glob, recurse );
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
        };

        try
        {
            new ClassSpaceScanner( brokenLoadSpace ).accept( new QualifiedTypeVisitor( null ) );
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }
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
