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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Qualifier;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sonatype.guice.bean.reflect.ClassSpace;

import com.google.inject.Binder;
import com.google.inject.Module;

public final class QualifiedScannerModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int CLASS_READER_FLAGS =
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    static final String QUALIFIER_DESC = Type.getDescriptor( Qualifier.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedScannerModule( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final ClassVisitor visitor = new QualifiedClassVisitor( binder );
        try
        {
            final Enumeration<URL> e = space.findEntries( null, "*.class", true );
            while ( e.hasMoreElements() )
            {
                try
                {
                    visitClass( e.nextElement(), visitor );
                }
                catch ( final Throwable t )
                {
                    binder.addError( t );
                }
            }
        }
        catch ( final Throwable t )
        {
            binder.addError( t );
        }
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    static void visitClass( final URL url, final ClassVisitor visitor )
        throws IOException
    {
        final InputStream in = url.openStream();
        try
        {
            new ClassReader( in ).accept( visitor, CLASS_READER_FLAGS );
        }
        finally
        {
            try
            {
                in.close();
            }
            catch ( final IOException e ) // NOPMD
            {
                // ignore
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    final class QualifiedClassVisitor
        extends EmptyClassVisitor
    {
        private final QualifiedClassBinder qualifiedClassBinder;

        private final QualifiedAnnotationVisitor qualifiedAnnotationVisitor = new QualifiedAnnotationVisitor();

        private final Map<String, Boolean> qualifiedAnnotationMap = new HashMap<String, Boolean>();

        private String clazzName;

        boolean isQualifiedClass;

        public QualifiedClassVisitor( final Binder binder )
        {
            qualifiedClassBinder = new QualifiedClassBinder( binder );
        }

        @Override
        public void visit( final int version, final int access, final String name, final String signature,
                           final String superName, final String[] interfaces )
        {
            if ( ( access & ( Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_SYNTHETIC ) ) != 0 )
            {
                return;
            }
            if ( null == clazzName )
            {
                clazzName = name;
            }
            if ( superName.contains( "Abstract" ) )
            {
                try
                {
                    visitClass( space.getResource( superName + ".class" ), this );
                }
                catch ( final IOException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
        {
            if ( null == clazzName || isQualifiedClass )
            {
                return null;
            }
            final Boolean isQualifiedAnnotation = qualifiedAnnotationMap.get( desc );
            if ( isQualifiedAnnotation != null )
            {
                isQualifiedClass = isQualifiedAnnotation.booleanValue();
                return null;
            }
            final String name = desc.substring( 1, desc.length() - 1 ) + ".class";
            try
            {
                visitClass( space.getResource( name ), qualifiedAnnotationVisitor );
                qualifiedAnnotationMap.put( desc, Boolean.valueOf( isQualifiedClass ) );
                return null;
            }
            catch ( final IOException e )
            {
                throw new RuntimeException( e );
            }
        }

        @Override
        public void visitEnd()
        {
            try
            {
                if ( isQualifiedClass )
                {
                    isQualifiedClass = false;
                    qualifiedClassBinder.bind( space.loadClass( clazzName.replace( '/', '.' ) ) );
                }
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( clazzName, e );
            }
            finally
            {
                clazzName = null;
            }
        }

        final class QualifiedAnnotationVisitor
            extends EmptyClassVisitor
        {
            @Override
            public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
            {
                isQualifiedClass |= QUALIFIER_DESC.equals( desc );
                return null;
            }
        }
    }
}
