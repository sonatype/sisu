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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.PrivateBinder;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.StaticInjectionRequest;

/**
 * {@link ElementVisitor} that analyzes linked {@link Binding}s for non-local injection dependencies.
 */
final class ElementAnalyzer
    extends DefaultElementVisitor<Void>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Key<?>> localKeys = new HashSet<Key<?>>();

    private final DependencyAnalyzer analyzer = new DependencyAnalyzer();

    private final List<ElementAnalyzer> privateAnalyzers = new ArrayList<ElementAnalyzer>();

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ElementAnalyzer( final Binder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void apply( final Wiring wiring )
    {
        // calculate which dependencies are missing from the module
        final Set<Key<?>> missingKeys = analyzer.getRequiredKeys();
        missingKeys.removeAll( localKeys );

        for ( final Key<?> key : missingKeys )
        {
            wiring.wire( key );
        }

        for ( final ElementAnalyzer privateAnalyzer : privateAnalyzers )
        {
            // can see parent's local/wired dependencies
            privateAnalyzer.localKeys.addAll( localKeys );
            privateAnalyzer.localKeys.addAll( missingKeys );
            privateAnalyzer.apply( wiring );
        }
    }

    @Override
    public <T> Void visit( final Binding<T> binding )
    {
        if ( localKeys.contains( binding.getKey() ) )
        {
            return null;
        }
        if ( binding.acceptTargetVisitor( analyzer ).booleanValue() )
        {
            localKeys.add( binding.getKey() );
            binding.applyTo( binder );
        }
        return null;
    }

    @Override
    public Void visit( final PrivateElements elements )
    {
        // based on private module introspection code from the Guice SPI
        final PrivateBinder privateBinder = binder.withSource( elements.getSource() ).newPrivateBinder();
        final ElementAnalyzer privateAnalyzer = new ElementAnalyzer( privateBinder );

        privateAnalyzers.add( privateAnalyzer );

        // let child know what bindings we have so far
        privateAnalyzer.localKeys.addAll( localKeys );
        for ( final Element e : elements.getElements() )
        {
            e.acceptVisitor( privateAnalyzer );
        }

        for ( final Key<?> k : elements.getExposedKeys() )
        {
            // only expose valid bindings that won't conflict with existing ones
            if ( privateAnalyzer.localKeys.contains( k ) && localKeys.add( k ) )
            {
                privateBinder.withSource( elements.getExposedSource( k ) ).expose( k );
            }
        }

        return null;
    }

    @Override
    public Void visit( final StaticInjectionRequest request )
    {
        analyzer.visit( request );
        request.applyTo( binder );
        return null;
    }

    @Override
    public Void visit( final InjectionRequest<?> request )
    {
        analyzer.visit( request );
        request.applyTo( binder );
        return null;
    }

    @Override
    public Void visitOther( final Element element )
    {
        element.applyTo( binder );
        return null;
    }
}
