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
import java.util.Map.Entry;

import javax.inject.Named;

import org.sonatype.guice.plexus.config.Hints;

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

    InjectorBeans( final Injector injector, final TypeLiteral<T> role )
    {
        this.injector = injector;

        if ( null == injector.getParent() )
        {
            try
            {
                // special case for root injector - try to include the default "just-in-time" binding
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
            else if ( null == ann && null != injector.getParent() )
            {
                // explicit default bindings must appear at the start: but only need
                // to do this for child injectors, as root injector is handled above
                add( 0, new LazyBean<T>( Hints.DEFAULT_HINT, binding.getProvider() ) );
            }
        }

        trimToSize(); // minimize memory usage
    }
}
