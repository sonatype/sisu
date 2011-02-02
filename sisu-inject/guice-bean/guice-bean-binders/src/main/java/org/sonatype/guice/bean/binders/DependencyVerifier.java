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
package org.sonatype.guice.bean.binders;

import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.guice.bean.reflect.DeclaredMembers;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.UntargettedBinding;

final class DependencyVerifier<T>
    extends DefaultBindingTargetVisitor<T, Void>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Class<?>> visited = new HashSet<Class<?>>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Void visit( final UntargettedBinding<? extends T> binding )
    {
        return verify( binding.getKey().getTypeLiteral() );
    }

    @Override
    public Void visit( final LinkedKeyBinding<? extends T> binding )
    {
        return verify( binding.getLinkedKey().getTypeLiteral() );
    }

    @Override
    public Void visit( final ConstructorBinding<? extends T> binding )
    {
        return verify( binding.getConstructor().getDeclaringType() );
    }

    @Override
    public Void visitOther( final Binding<? extends T> binding )
    {
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Void verify( final TypeLiteral<?> type )
    {
        Class<?> clazz = type.getRawType();
        if ( visited.add( clazz ) )
        {
            for ( final Member m : new DeclaredMembers( type.getRawType() ) )
            {
                final Class<?> mClazz = m.getDeclaringClass();
                if ( clazz != mClazz && !visited.add( mClazz ) )
                {
                    break;
                }
                clazz = mClazz;
            }
        }
        return null;
    }
}
