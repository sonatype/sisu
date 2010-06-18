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

import org.sonatype.guice.bean.locators.HiddenSource;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

/**
 * {@link List} of Plexus beans with the same type from the same {@link Injector}.
 */
final class InjectorBeans<T>
    extends ArrayList<PlexusBeanLocator.Bean<T>>
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

        // only covers explicit bindings, such as those specified in a module
        for ( final Binding<T> binding : injector.findBindingsByType( role ) )
        {
            if ( binding.getSource() instanceof HiddenSource )
            {
                continue; // ignore any hidden bindings
            }
            final Annotation ann = binding.getKey().getAnnotation();
            if ( null == ann )
            {
                add( 0, new LazyPlexusBean<T>( binding ) ); // default takes precedence
            }
            else if ( ann instanceof Named && !Hints.isDefaultHint( ( (Named) ann ).value() ) )
            {
                add( new LazyPlexusBean<T>( binding ) );
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
        super( hints.length ); // minimize memory usage

        this.injector = injector;

        // only covers explicit bindings, such as those specified in a module
        final Map<Key<?>, Binding<?>> bindings = injector.getBindings();
        for ( final String hint : hints )
        {
            final Binding binding = bindings.get( Roles.componentKey( role, hint ) );
            if ( null != binding && false == binding.getSource() instanceof HiddenSource )
            {
                add( new LazyPlexusBean<T>( binding ) );
            }
        }
    }
}
