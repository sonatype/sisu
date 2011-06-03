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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;

import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.TypeLiteral;

/**
 * Source of "implicit" bindings; includes @{@link ImplementedBy}, @{@link ProvidedBy}, and concrete types.
 */
final class ImplicitBindings
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Injector> injectors;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ImplicitBindings( final BeanLocator locator )
    {
        injectors = new EntryListAdapter<Annotation, Injector>( locator.locate( Key.get( Injector.class ) ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public <T> Binding<T> get( final TypeLiteral<T> type )
    {
        final Key implicitKey = Key.get( type.getRawType(), Implicit.class );
        for ( final Injector i : injectors )
        {
            try
            {
                // first round: check for any re-written implicit bindings
                final Binding binding = i.getBindings().get( implicitKey );
                if ( null != binding )
                {
                    return binding;
                }
            }
            catch ( final Throwable e )
            {
                continue; // no luck, move onto next injector
            }
        }

        final Key justInTimeKey = Key.get( type );
        for ( final Injector i : injectors )
        {
            try
            {
                // second round: fall back to just-in-time lookup
                final Binding binding = i.getBinding( justInTimeKey );
                if ( InjectorPublisher.isVisible( binding ) )
                {
                    return binding;
                }
            }
            catch ( final Throwable e )
            {
                continue; // no luck, move onto next injector
            }
        }
        return null;
    }
}
