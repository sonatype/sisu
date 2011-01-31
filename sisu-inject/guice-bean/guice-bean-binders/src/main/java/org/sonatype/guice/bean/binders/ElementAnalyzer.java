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

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.InjectionRequest;

/**
 * {@link ElementVisitor} that analyzes linked {@link Binding}s for non-local injection dependencies.
 */
final class ElementAnalyzer
    extends DefaultElementVisitor<Void>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final DependencyAnalyzer<Object> DEPENDENCY_ANALYZER = new DependencyAnalyzer<Object>();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Key<?>> localKeys = new HashSet<Key<?>>();

    private final Set<Key<?>> importedKeys = new HashSet<Key<?>>();

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ElementAnalyzer( final Binder binder )
    {
        this.binder = binder;

        // properties parameter is implicitly required
        importedKeys.add( ParameterKeys.PROPERTIES );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return Imported (non-local) dependency keys
     */
    public Set<Key<?>> getImportedKeys()
    {
        // finally remove all local bindings
        importedKeys.removeAll( localKeys );

        return importedKeys;
    }

    @Override
    public <T> Void visit( final Binding<T> binding )
    {
        // silently remove duplicate bindings
        if ( localKeys.add( binding.getKey() ) )
        {
            binding.applyTo( binder );
            importedKeys.addAll( binding.acceptTargetVisitor( DEPENDENCY_ANALYZER ) );
        }
        return null;
    }

    @Override
    public Void visit( final InjectionRequest<?> injectionRequest )
    {
        injectionRequest.applyTo( binder );
        importedKeys.addAll( DependencyAnalyzer.analyze( injectionRequest.getType() ) );
        return null;
    }

    @Override
    public Void visitOther( final Element element )
    {
        element.applyTo( binder );
        return null;
    }
}
