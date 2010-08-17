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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import javax.inject.Provider;

import com.google.inject.Binding;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.spi.DefaultBindingScopingVisitor;

/**
 * Qualified bean {@link Entry} and {@link Provider} backed by an injector {@link Binding}.
 */
public final class QualifiedBean<Q extends Annotation, T>
    extends DefaultBindingScopingVisitor<Provider<T>>
    implements Entry<Q, T>, Provider<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final ImplementationVisitor IMPLEMENTATION_VISITOR = new ImplementationVisitor();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Q qualifier;

    private final Binding<T> binding;

    private final Provider<T> provider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedBean( final Q qualifier, final Binding<T> binding )
    {
        this.qualifier = qualifier;
        this.binding = binding;

        this.provider = binding.acceptScopingVisitor( this );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return Qualified bean binding
     */
    public Binding<? extends T> getBinding()
    {
        return binding;
    }

    @SuppressWarnings( "unchecked" )
    public Class<T> getImplementationClass()
    {
        return (Class<T>) binding.acceptTargetVisitor( IMPLEMENTATION_VISITOR );
    }

    public Q getKey()
    {
        return qualifier;
    }

    public T getValue()
    {
        return provider.get();
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public T get()
    {
        return getValue();
    }

    @Override
    public String toString()
    {
        return getKey() + "=" + getValue();
    }

    @Override
    public Provider<T> visitEagerSingleton()
    {
        return binding.getProvider();
    }

    @Override
    public Provider<T> visitScope( final Scope scope )
    {
        return Scopes.SINGLETON.equals( scope ) ? visitEagerSingleton() : visitOther();
    }

    @Override
    public Provider<T> visitScopeAnnotation( final Class<? extends Annotation> scopeAnnotation )
    {
        return "Singleton".equals( scopeAnnotation.getSimpleName() ) ? visitEagerSingleton() : visitOther();
    }

    @Override
    protected Provider<T> visitOther()
    {
        return Scopes.SINGLETON.scope( binding.getKey(), binding.getProvider() );
    }
}
