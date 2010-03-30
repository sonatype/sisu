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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Qualifier;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sonatype.guice.bean.reflect.ClassSpace;

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

    private final List<Class<?>> qualifiedTypes = new ArrayList<Class<?>>();

    private final ClassSpace space;

    private String clazzName;

    boolean isQualified;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedClassVisitor( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        if ( ( access & Opcodes.ACC_INTERFACE ) != 0 )
        {
            return; // we don't scan the interface hierarchy
        }
        if ( null == clazzName && ( access & Opcodes.ACC_ABSTRACT ) == 0 )
        {
            clazzName = name; // primary class must be concrete type
        }
        if ( isQualified || null == clazzName || null == superName )
        {
            return; // short-cut scanning process
        }

        /*
         * scan for qualified super-classes and cache the results
         */
        final Boolean isQualifiedClass = qualifiedClassMap.get( superName );
        if ( isQualifiedClass != null )
        {
            isQualified = isQualifiedClass.booleanValue();
        }
        else if ( !superName.startsWith( "java" ) )
        {
            try
            {
                visitClass( space.getResource( superName + ".class" ), this );
                qualifiedClassMap.put( superName, Boolean.valueOf( isQualified ) );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException( e.toString() );
            }
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        if ( isQualified || null == clazzName )
        {
            return null; // short-cut scanning process
        }

        /*
         * scan for qualified annotations and cache the results
         */
        final Boolean isQualifiedAnnotation = qualifiedAnnotationMap.get( desc );
        if ( isQualifiedAnnotation != null )
        {
            isQualified = isQualifiedAnnotation.booleanValue();
        }
        else
        {
            try
            {
                final String annotationClazz = desc.substring( 1, desc.length() - 1 ) + ".class";
                visitClass( space.getResource( annotationClazz ), qualifiedAnnotationVisitor );
                qualifiedAnnotationMap.put( desc, Boolean.valueOf( isQualified ) );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException( e.toString() );
            }
        }

        return null;
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    void scan( final URL url )
        throws IOException
    {
        isQualified = false;
        clazzName = null;

        visitClass( url, this );

        if ( isQualified )
        {
            try
            {
                qualifiedTypes.add( space.loadClass( clazzName.replace( '/', '.' ) ) );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( clazzName, e );
            }
        }
    }

    List<Class<?>> getQualifiedTypes()
    {
        return qualifiedTypes;
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
        if ( null == url )
        {
            return; // missing class definition
        }
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
