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

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * 
 */
final class HintedInjectorBeans<T>
    extends ArrayList<Entry<String, T>>
    implements InjectorBeans<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HintedInjectorBeans( final Injector injector, final TypeLiteral<T> role, final String[] hints )
    {
        super( hints.length );
        boolean isEmpty = true;

        if ( null == injector.getParent() )
        {
            for ( final String h : hints )
            {
                try
                {
                    add( new LazyBean<T>( h, injector.getProvider( Roles.componentKey( role, h ) ) ) );
                    isEmpty = false;
                }
                catch ( final ConfigurationException e )
                {
                    add( null );
                }
            }
        }
        else
        {
            final Map<Key<?>, Binding<?>> bindingMap = injector.getBindings();
            for ( final String h : hints )
            {
                @SuppressWarnings( "unchecked" )
                final Binding<T> binding = (Binding) bindingMap.get( Roles.componentKey( role, h ) );
                if ( null != binding )
                {
                    add( new LazyBean<T>( h, binding.getProvider() ) );
                    isEmpty = false;
                }
                else
                {
                    add( null );
                }
            }
        }

        if ( isEmpty )
        {
            clear();
        }

        this.injector = injector;
    }

    // ----------------------------------------------------------------------
    // Public fields
    // ----------------------------------------------------------------------

    public Injector injector()
    {
        return injector;
    }
}
