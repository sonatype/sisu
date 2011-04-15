/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;

class DelegatingBinding<T>
    implements Binding<T>
{
    private final Key<T> key;

    private final Binding<T> binding;

    DelegatingBinding( final Key<T> key, final Binding<T> binding )
    {
        this.key = key;
        this.binding = binding;
    }

    public Key<T> getKey()
    {
        return key;
    }

    public Provider<T> getProvider()
    {
        return binding.getProvider();
    }

    public Object getSource()
    {
        return binding.getSource();
    }

    public void applyTo( final Binder binder )
    {
        binder.bind( getKey() ).toProvider( getProvider() );
    }

    public <V> V acceptVisitor( final ElementVisitor<V> visitor )
    {
        return binding.acceptVisitor( visitor );
    }

    public <V> V acceptScopingVisitor( final BindingScopingVisitor<V> visitor )
    {
        return binding.acceptScopingVisitor( visitor );
    }

    public <V> V acceptTargetVisitor( final BindingTargetVisitor<? super T, V> visitor )
    {
        return binding.acceptTargetVisitor( visitor );
    }
}