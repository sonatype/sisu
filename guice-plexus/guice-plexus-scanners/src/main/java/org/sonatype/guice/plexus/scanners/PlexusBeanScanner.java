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

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.scanners.EmptyAnnotationVisitor;
import org.sonatype.guice.bean.scanners.QualifiedBeanScanner;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.Strategies;

/**
 * {@link BeanScanner} that records Plexus bean classes annotated with @{@link Component}.
 */
public class PlexusBeanScanner
    extends QualifiedBeanScanner
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String COMPONENT_DESC = Type.getDescriptor( Component.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final AnnotationVisitor componentVisitor = new ComponentAnnotationVisitor();

    private final PlexusComponentRegistry registry;

    private String role;

    private String hint;

    private String instantiationStrategy;

    private String description;

    private String implementation;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    protected PlexusBeanScanner( final PlexusComponentRegistry registry )
    {
        super( registry );

        this.registry = registry;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final Map<Component, DeferredClass<?>> getComponents()
    {
        return registry.getComponents();
    }

    public void setRole( final String role )
    {
        this.role = role;
    }

    public void setHint( final String hint )
    {
        this.hint = hint;
    }

    public final String getRole()
    {
        return role;
    }

    public final String getHint()
    {
        return Hints.canonicalHint( hint );
    }

    public void setInstantiationStrategy( final String instantiationStrategy )
    {
        this.instantiationStrategy = instantiationStrategy;
    }

    public void setDescription( final String description )
    {
        this.description = description;
    }

    public void setImplementation( final String implementation )
    {
        this.implementation = implementation;
    }

    @Override
    public void visit( final ClassSpace space )
    {
        role = null;
        hint = Hints.DEFAULT_HINT;
        instantiationStrategy = Strategies.SINGLETON;
        description = "";

        super.visit( space );
    }

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        if ( ( access & ( Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_SYNTHETIC ) ) == 0 )
        {
            setImplementation( name.replace( '/', '.' ) );
        }
        super.visit( version, access, name, signature, superName, interfaces );
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        if ( COMPONENT_DESC.equals( desc ) )
        {
            return componentVisitor;
        }
        return super.visitAnnotation( desc, visible );
    }

    @Override
    public void visitEnd()
    {
        if ( null != role )
        {
            registry.addComponent( role, hint, instantiationStrategy, description, implementation );
            role = null;
        }
        else
        {
            super.visitEnd();
        }
    }

    // ----------------------------------------------------------------------
    // Component annotation scanner
    // ----------------------------------------------------------------------

    final class ComponentAnnotationVisitor
        extends EmptyAnnotationVisitor
    {
        @Override
        public void visit( final String name, final Object value )
        {
            if ( "role".equals( name ) )
            {
                setRole( ( (Type) value ).getClassName() );
            }
            else if ( "hint".equals( name ) )
            {
                setHint( (String) value );
            }
            else if ( "instantiationStrategy".equals( name ) )
            {
                setInstantiationStrategy( (String) value );
            }
            else if ( "description".equals( name ) )
            {
                setDescription( (String) value );
            }
        }
    }
}
