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

/**
 * {@link ClassVisitor} that detects beans annotated with {@link Qualifier} annotations.
 */
final class QualifiedClassVisitor
    extends EmptyClassVisitor
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

    private final QualifiedAnnotationVisitor qualifiedAnnotationVisitor = new QualifiedAnnotationVisitor();

    private final Map<String, Boolean> qualifiedAnnotationMap = new HashMap<String, Boolean>();

    private final Map<String, Boolean> qualifiedClassMap = new HashMap<String, Boolean>();

    private final ClassSpace space;

    private final QualifiedClassBinder qualifiedClassBinder;

    private String clazzName;

    boolean isQualified;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedClassVisitor( final ClassSpace space, final Binder binder )
    {
        this.space = space;

        qualifiedClassBinder = new QualifiedClassBinder( binder );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        if ( isQualified || ( access & ( Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_SYNTHETIC ) ) != 0 )
        {
            return; // no need to process this class
        }
        if ( null == clazzName )
        {
            clazzName = name; // primary class name
        }

        // look for the qualifier on the superclass and cache the results
        final Boolean isQualifiedClass = qualifiedClassMap.get( superName );
        if ( isQualifiedClass != null )
        {
            isQualified = isQualifiedClass.booleanValue();
        }
        else if ( superName != null && !superName.startsWith( "java" ) )
        {
            try
            {
                visitClass( space.getResource( superName + ".class" ), this );
                qualifiedClassMap.put( superName, Boolean.valueOf( isQualified ) );
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
        if ( isQualified || null == clazzName )
        {
            return null; // no need to process this annotation
        }

        // look for the qualifier marker on the annotation and cache the results
        final Boolean isQualifiedAnnotation = qualifiedAnnotationMap.get( desc );
        if ( isQualifiedAnnotation != null )
        {
            isQualified = isQualifiedAnnotation.booleanValue();
            return null;
        }
        final String path = desc.substring( 1, desc.length() - 1 ) + ".class";
        try
        {
            visitClass( space.getResource( path ), qualifiedAnnotationVisitor );
            qualifiedAnnotationMap.put( desc, Boolean.valueOf( isQualified ) );
            return null;
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    void scan( final URL url )
        throws IOException
    {
        // scan primary class
        visitClass( url, this );
        final String qualifiedName = clazzName;
        clazzName = null;

        if ( isQualified )
        {
            isQualified = false;
            try
            {
                // load actual bean class (provides type-safety) and register the right bindings
                qualifiedClassBinder.bind( space.loadClass( qualifiedName.replace( '/', '.' ) ) );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( qualifiedName, e );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    final class QualifiedAnnotationVisitor
        extends EmptyClassVisitor
    {
        @Override
        public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
        {
            isQualified |= QUALIFIER_DESC.equals( desc );
            return null;
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static void visitClass( final URL url, final ClassVisitor visitor )
        throws IOException
    {
        if ( null != url )
        {
            final InputStream in = url.openStream();
            try
            {
                new ClassReader( in ).accept( visitor, CLASS_READER_FLAGS );
            }
            finally
            {
                in.close();
            }
        }
    }
}
