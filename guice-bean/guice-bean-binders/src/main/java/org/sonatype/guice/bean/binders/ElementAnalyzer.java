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
package org.sonatype.guice.bean.binders;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.guice.bean.locators.BeanLocator;

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

        // must avoid adding an import for the locator
        localKeys.add( Key.get( BeanLocator.class ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return Imported (non-local) dependency keys
     */
    public Set<Key<?>> getImportedKeys()
    {
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
            importedKeys.removeAll( localKeys );
        }
        return null;
    }

    @Override
    public Void visit( final InjectionRequest<?> injectionRequest )
    {
        injectionRequest.applyTo( binder );
        importedKeys.addAll( DependencyAnalyzer.analyze( injectionRequest.getType() ) );
        importedKeys.removeAll( localKeys );
        return null;
    }

    @Override
    public Void visitOther( final Element element )
    {
        element.applyTo( binder );
        return null;
    }
}