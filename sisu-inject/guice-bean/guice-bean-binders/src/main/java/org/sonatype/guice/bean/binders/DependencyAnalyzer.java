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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Provider;

import org.sonatype.guice.bean.reflect.DeferredProvider;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.StaticInjectionRequest;
import com.google.inject.spi.UntargettedBinding;

/**
 * {@link BindingTargetVisitor} that collects the {@link Key}s of any injected dependencies.
 */
final class DependencyAnalyzer
    extends DefaultBindingTargetVisitor<Object, Boolean>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<TypeLiteral<?>> processedTypes = new HashSet<TypeLiteral<?>>();

    private final Set<Key<?>> requiredKeys = new HashSet<Key<?>>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DependencyAnalyzer()
    {
        // properties parameter is implicitly required
        requiredKeys.add( ParameterKeys.PROPERTIES );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Set<Key<?>> getRequiredKeys()
    {
        return requiredKeys;
    }

    @Override
    public Boolean visit( final UntargettedBinding<?> binding )
    {
        return analyze( binding.getKey().getTypeLiteral() );
    }

    @Override
    public Boolean visit( final LinkedKeyBinding<?> binding )
    {
        return analyze( binding.getLinkedKey().getTypeLiteral() );
    }

    @Override
    public Boolean visit( final ConstructorBinding<?> binding )
    {
        return collect( binding.getDependencies() );
    }

    @Override
    public Boolean visit( final InstanceBinding<?> binding )
    {
        return collect( binding.getDependencies() );
    }

    @Override
    public Boolean visit( final ProviderInstanceBinding<?> binding )
    {
        final Provider<?> provider = binding.getProviderInstance();
        if ( provider instanceof DeferredProvider<?> )
        {
            try
            {
                final DeferredProvider<?> deferredProvider = (DeferredProvider<?>) provider;
                analyze( TypeLiteral.get( deferredProvider.getImplementationClass().load() ) );
            }
            catch ( final Throwable e ) // NOPMD
            {
                // deferred provider, so ignore errors for now
            }
            return Boolean.TRUE;
        }
        return collect( binding.getDependencies() );
    }

    public Boolean visit( final StaticInjectionRequest request )
    {
        for ( final InjectionPoint p : request.getInjectionPoints() )
        {
            collect( p.getDependencies() );
        }
        return Boolean.TRUE;
    }

    public Boolean visit( final InjectionRequest<?> request )
    {
        for ( final InjectionPoint p : request.getInjectionPoints() )
        {
            collect( p.getDependencies() );
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitOther( final Binding<?> binding )
    {
        return Boolean.TRUE;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Boolean analyze( final TypeLiteral<?> type )
    {
        if ( ( type.getRawType().getModifiers() & ( Modifier.INTERFACE | Modifier.ABSTRACT ) ) != 0 )
        {
            return Boolean.TRUE;
        }
        try
        {
            if ( !processedTypes.contains( type ) )
            {
                collect( InjectionPoint.forConstructorOf( type ).getDependencies() );
                for ( final InjectionPoint p : InjectionPoint.forInstanceMethodsAndFields( type ) )
                {
                    collect( p.getDependencies() );
                }
                processedTypes.add( type );
            }
            return Boolean.TRUE;
        }
        catch ( final Throwable e )
        {
            return Boolean.FALSE;
        }
    }

    private Boolean collect( final Collection<Dependency<?>> dependencies )
    {
        for ( final Dependency<?> d : dependencies )
        {
            requiredKeys.add( d.getKey() );
        }
        return Boolean.TRUE;
    }
}
