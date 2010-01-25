/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.scanners;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;

/**
 * {@link ClassVisitor} that collects Plexus beans annotated with @{@link Component}.
 */
final class PlexusComponentClassVisitor
    implements ClassVisitor, AnnotationVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String COMPONENT_DESC = Type.getDescriptor( Component.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<Component, DeferredClass<?>> components = new HashMap<Component, DeferredClass<?>>();

    private final Logger logger = LoggerFactory.getLogger( PlexusComponentClassVisitor.class );

    private String role;

    private String hint;

    private String instantiationStrategy;

    private String description;

    private String implementation;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusComponentClassVisitor( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        if ( ( access & ( Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_SYNTHETIC ) ) != 0 )
        {
            return;
        }

        role = null;
        hint = Hints.DEFAULT_HINT;
        instantiationStrategy = "singleton";
        description = "";

        implementation = name.replace( '/', '.' );
    }

    public void visitSource( final String source, final String debug )
    {
    }

    public void visitOuterClass( final String owner, final String name, final String desc )
    {
    }

    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        return COMPONENT_DESC.equals( desc ) ? this : null;
    }

    public void visitAttribute( final Attribute attr )
    {
    }

    public void visitInnerClass( final String name, final String outerName, final String innerName, final int access )
    {
    }

    public FieldVisitor visitField( final int access, final String name, final String desc, final String signature,
                                    final Object value )
    {
        return null;
    }

    public MethodVisitor visitMethod( final int access, final String name, final String desc, final String signature,
                                      final String[] exceptions )
    {
        return null;
    }

    public void visit( final String name, final Object value )
    {
        if ( "role".equals( name ) )
        {
            role = ( (Type) value ).getClassName();
        }
        else if ( "hint".equals( name ) )
        {
            hint = Hints.canonicalHint( (String) value );
        }
        else if ( "instantiationStrategy".equals( name ) )
        {
            instantiationStrategy = (String) value;
        }
        else if ( "description".equals( name ) )
        {
            description = (String) value;
        }
    }

    public void visitEnum( final String name, final String desc, final String value )
    {
    }

    public AnnotationVisitor visitAnnotation( final String name, final String desc )
    {
        return null;
    }

    public AnnotationVisitor visitArray( final String name )
    {
        return null;
    }

    public void visitEnd()
    {
        if ( null == role )
        {
            return;
        }

        final Class<?> clazz;
        try
        {
            // check the role actually exists
            clazz = space.loadClass( role );
        }
        catch ( final Throwable e )
        {
            // not all roles are used, so just note for now
            logger.debug( "Missing Plexus role: " + role, e );
            return;
        }

        final Component component = new ComponentImpl( clazz, hint, instantiationStrategy, description );
        components.put( component, space.deferLoadClass( implementation ) );
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    Map<Component, DeferredClass<?>> getComponents()
    {
        return components;
    }
}