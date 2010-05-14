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
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Qualifier;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sonatype.guice.bean.reflect.ClassSpace;

import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * {@link BeanScanner} that detects types annotated with {@link Qualifier} annotations.
 */
public class QualifiedBeanScanner
    extends EmptyClassVisitor
    implements ClassSpaceVisitor
{
    private static final com.google.inject.name.Named DEFAULT_NAMED = Names.named( "default" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final QualifierTypes qualifierTypes = new QualifierTypes();

    private final QualifiedBeanRegistry registry;

    private ClassSpace space;

    private String beanName;

    private Class<? extends Annotation> qualifierType;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedBeanScanner( final QualifiedBeanRegistry registry )
    {
        this.registry = registry;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final ClassSpace _space )
    {
        space = _space;
    }

    public ClassVisitor visitClass()
    {
        beanName = null;
        qualifierType = null;
        return this;
    }

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        if ( ( access & ( Opcodes.ACC_INTERFACE | access & Opcodes.ACC_ABSTRACT ) ) == 0 )
        {
            beanName = name;
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        if ( null != beanName && null == qualifierType )
        {
            qualifierType = qualifierTypes.qualify( space, desc );
        }
        return null;
    }

    @Override
    public void visitEnd()
    {
        if ( null != qualifierType )
        {
            final Class<?> beanType = load( space, beanName );
            registry.add( getKey( beanType, qualifierType ), beanType );
            qualifierType = null;
        }
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    static final class QualifierTypes
        extends EmptyClassVisitor
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final String QUALIFIER_DESC = Type.getDescriptor( Qualifier.class );

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        private final Map<String, Class> cachedResults = new HashMap<String, Class>();

        private boolean isQualified;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        public Class<? extends Annotation> qualify( final ClassSpace space, final String desc )
        {
            if ( !cachedResults.containsKey( desc ) )
            {
                try
                {
                    final String name = desc.substring( 1, desc.length() - 1 );
                    ClassSpaceScanner.accept( this, space.getResource( name + ".class" ) );
                    cachedResults.put( desc, isQualified ? load( space, name ) : null );
                }
                catch ( final IOException e )
                {
                    throw new RuntimeException( e.toString() );
                }
            }
            return cachedResults.get( desc );
        }

        @Override
        public void visit( final int version, final int access, final String name, final String signature,
                           final String superName, final String[] interfaces )
        {
            isQualified = false;
        }

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

    static final Class<?> load( final ClassSpace space, final String name )
    {
        try
        {
            return space.loadClass( name.replace( '/', '.' ) );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new TypeNotPresentException( name, e );
        }
    }

    private static Class<?> getBindingType( final Class<?> beanType )
    {
        Class<?> ancestor = beanType;
        for ( Class<?> clazz = beanType; clazz != Object.class; ancestor = clazz, clazz = clazz.getSuperclass() )
        {
            final Typed typed = clazz.getAnnotation( Typed.class );
            final Class<?>[] interfaces = null != typed ? typed.value() : clazz.getInterfaces();
            if ( interfaces.length == 1 )
            {
                return interfaces[0];
            }
            if ( interfaces.length > 1 )
            {
                throw new IllegalArgumentException( beanType + " has multiple interfaces, use @Typed to pick one" );
            }
        }
        return ancestor;
    }

    private static Key<?> getKey( final Class<?> beanType, final Class<? extends Annotation> qualifierType )
    {
        Annotation qualifier = beanType.getAnnotation( qualifierType );

        final String hint;
        if ( qualifierType.equals( javax.inject.Named.class ) )
        {
            hint = ( (javax.inject.Named) qualifier ).value();
        }
        else if ( qualifierType.equals( com.google.inject.name.Named.class ) )
        {
            hint = ( (com.google.inject.name.Named) qualifier ).value();
        }
        else
        {
            hint = "CUSTOM";
        }

        if ( hint.length() == 0 )
        {
            // Assume Default* types are default implementations
            if ( beanType.getSimpleName().startsWith( "Default" ) )
            {
                qualifier = DEFAULT_NAMED;
            }
            else
            {
                qualifier = Names.named( beanType.getName() );
            }
        }
        else if ( "default".equalsIgnoreCase( hint ) )
        {
            qualifier = DEFAULT_NAMED;
        }

        return Key.get( getBindingType( beanType ), qualifier );
    }
}
