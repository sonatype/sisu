/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.locators;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Named;

import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * {@link List} of Plexus beans with the same type from the same Guice {@link Injector}.
 */
final class InjectorBeans<T>
    extends ArrayList<Entry<String, T>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final transient Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a list of all beans with the given role type from the given injector.
     * 
     * @param injector The injector
     * @param role The Plexus role
     */
    InjectorBeans( final Injector injector, final TypeLiteral<T> role )
    {
        this.injector = injector;

        final Injector parent = injector.getParent();
        if ( null == parent )
        {
            try
            {
                // special case for root injector - try to include any default "just-in-time" binding
                add( new LazyBean<T>( Hints.DEFAULT_HINT, injector.getProvider( Key.get( role ) ) ) );
            }
            catch ( final ConfigurationException e ) // NOPMD
            {
                // default doesn't always exist, so drop-through and search named hints
            }
        }

        // only covers explicit bindings, such as those specified in a module
        for ( final Binding<T> binding : injector.findBindingsByType( role ) )
        {
            final Annotation ann = binding.getKey().getAnnotation();
            if ( ann instanceof Named )
            {
                final String hint = ( (Named) ann ).value();
                if ( !Hints.isDefaultHint( hint ) )
                {
                    add( new LazyBean<T>( hint, binding.getProvider() ) );
                }
            }
            else if ( null == ann && null != parent )
            {
                // explicit default bindings must appear at the start: but only need
                // to do this for child injectors, as root injector is handled above
                add( 0, new LazyBean<T>( Hints.DEFAULT_HINT, binding.getProvider() ) );
            }
        }

        trimToSize(); // minimize memory usage
    }

    /**
     * Creates a list of beans with the given named hints and the given role type from the given injector.
     * 
     * @param injector The injector
     * @param role The Plexus role
     * @param hints The Plexus hints
     */
    @SuppressWarnings( "unchecked" )
    InjectorBeans( final Injector injector, final TypeLiteral<T> role, final String[] hints )
    {
        this.injector = injector;

        // use hints to look up the bindings directly
        final Injector parent = injector.getParent();
        final Map<Key<?>, Binding<?>> bindings = injector.getBindings();
        for ( final String hint : hints )
        {
            if ( null == parent && Hints.isDefaultHint( hint ) )
            {
                try
                {
                    // special case for root injector - try to include any default "just-in-time" binding
                    add( new LazyBean<T>( Hints.DEFAULT_HINT, injector.getProvider( Key.get( role ) ) ) );
                }
                catch ( final ConfigurationException e ) // NOPMD
                {
                    // default doesn't always exist, so move onto next hint
                }
            }
            else
            {
                // only covers explicit bindings, such as those specified in a module
                final Binding binding = bindings.get( Roles.componentKey( role, hint ) );
                if ( null != binding )
                {
                    add( new LazyBean<T>( hint, binding.getProvider() ) );
                }
            }
        }

        trimToSize(); // minimize memory usage
    }
}
