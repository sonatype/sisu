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

import org.sonatype.guice.bean.reflect.Logs;

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
    // Implementation fields
    // ----------------------------------------------------------------------

    private final DependencyAnalyzer<Object> analyzer = new DependencyAnalyzer<Object>();

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
        importedKeys.removeAll( localKeys );
        return importedKeys;
    }

    @Override
    public <T> Void visit( final Binding<T> binding )
    {
        final Key<T> key = binding.getKey();
        if ( !localKeys.contains( key ) )
        {
            try
            {
                importedKeys.addAll( binding.acceptTargetVisitor( analyzer ) );
                binding.applyTo( binder );
                localKeys.add( key );
            }
            catch ( final Throwable e )
            {
                Logs.debug( ElementAnalyzer.class, "Bad binding: {} cause: {}", binding, e );
            }
        }
        return null;
    }

    @Override
    public Void visit( final InjectionRequest<?> injectionRequest )
    {
        try
        {
            importedKeys.addAll( analyzer.visit( injectionRequest ) );
            injectionRequest.applyTo( binder );
        }
        catch ( final Throwable e )
        {
            Logs.debug( ElementAnalyzer.class, "Bad binding: {} cause: {}", injectionRequest, e );
        }
        return null;
    }

    @Override
    public Void visitOther( final Element element )
    {
        element.applyTo( binder );
        return null;
    }
}
