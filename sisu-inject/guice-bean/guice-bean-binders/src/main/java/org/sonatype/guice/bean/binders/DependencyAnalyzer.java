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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.sonatype.guice.bean.reflect.DeferredProvider;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
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

    private final Map<TypeLiteral<?>, Boolean> cachedResults = new HashMap<TypeLiteral<?>, Boolean>();

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
        return analyzeImplementation( binding.getKey().getTypeLiteral() );
    }

    @Override
    public Boolean visit( final LinkedKeyBinding<?> binding )
    {
        return analyzeImplementation( binding.getLinkedKey().getTypeLiteral() );
    }

    @Override
    public Boolean visit( final ProviderKeyBinding<?> binding )
    {
        return analyzeImplementation( binding.getProviderKey().getTypeLiteral() );
    }

    @Override
    public Boolean visit( final ProviderInstanceBinding<?> binding )
    {
        final Provider<?> provider = binding.getProviderInstance();
        if ( provider instanceof DeferredProvider<?> )
        {
            try
            {
                analyzeImplementation( TypeLiteral.get( ( (DeferredProvider<?>) provider ).getImplementationClass().load() ) );
            }
            catch ( final Throwable e ) // NOPMD
            {
                // deferred provider, so ignore errors for now
            }
            return Boolean.TRUE;
        }
        return Boolean.valueOf( analyzeDependencies( binding.getDependencies() ) );
    }

    @Override
    public Boolean visitOther( final Binding<?> binding )
    {
        if ( binding instanceof HasDependencies )
        {
            return Boolean.valueOf( analyzeDependencies( ( (HasDependencies) binding ).getDependencies() ) );
        }
        return Boolean.TRUE;
    }

    public Boolean visit( final StaticInjectionRequest request )
    {
        return Boolean.valueOf( analyzeInjectionPoints( request.getInjectionPoints() ) );
    }

    public Boolean visit( final InjectionRequest<?> request )
    {
        return Boolean.valueOf( analyzeInjectionPoints( request.getInjectionPoints() ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Boolean analyzeImplementation( final TypeLiteral<?> type )
    {
        Boolean applyBinding = cachedResults.get( type );
        if ( null == applyBinding )
        {
            boolean result = true;
            if ( ( type.getRawType().getModifiers() & ( Modifier.INTERFACE | Modifier.ABSTRACT ) ) == 0 )
            {
                try
                {
                    result = analyzeDependencies( InjectionPoint.forConstructorOf( type ).getDependencies() );
                    result &= analyzeInjectionPoints( InjectionPoint.forInstanceMethodsAndFields( type ) );
                }
                catch ( final Throwable e )
                {
                    result = false;
                }
            }
            applyBinding = Boolean.valueOf( result );
            cachedResults.put( type, applyBinding );
        }
        return applyBinding;
    }

    private boolean analyzeInjectionPoints( final Set<InjectionPoint> points )
    {
        boolean applyBinding = true;
        for ( final InjectionPoint p : points )
        {
            applyBinding &= analyzeDependencies( p.getDependencies() );
        }
        return applyBinding;
    }

    private boolean analyzeDependencies( final Collection<Dependency<?>> dependencies )
    {
        boolean applyBinding = true;
        for ( final Dependency<?> d : dependencies )
        {
            final Key<?> key = d.getKey();
            if ( key.hasAttributes() && "Assisted".equals( key.getAnnotationType().getSimpleName() ) )
            {
                applyBinding = false; // avoid directly binding AssistedInject based components
            }
            else
            {
                requiredKeys.add( key );
            }
        }
        return applyBinding;
    }
}
