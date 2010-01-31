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
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.guice.plexus.config.Hints;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * 
 */
final class BeanEntriesByRole<T>
    extends AbstractBeanEntries<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private int injectorIndex = -1;

    private int bindingIndex;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanEntriesByRole( final List<Injector> injectors, final TypeLiteral<T> role )
    {
        super( injectors, role );
    }

    // ----------------------------------------------------------------------
    // Cache methods
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings( "unchecked" )
    Entry<String, T> populate( final int index )
    {
        if ( -1 == injectorIndex )
        {
            try
            {
                final Provider defaultProvider = injectors.get( ++injectorIndex ).getProvider( Key.get( role ) );
                return new LazyBeanEntry( Hints.DEFAULT_HINT, defaultProvider );
            }
            catch ( final ConfigurationException e ) // NOPMD
            {
                // drop-through and search child injectors
            }
        }
        while ( injectorIndex < injectors.size() )
        {
            final Injector injector = injectors.get( injectorIndex );
            if ( null != injector )
            {
                final List<Binding<T>> bindings = injector.findBindingsByType( role );
                while ( bindingIndex < bindings.size() )
                {
                    final Binding binding = bindings.get( bindingIndex++ );
                    final Annotation ann = binding.getKey().getAnnotation();
                    if ( ann instanceof Named )
                    {
                        final String hint = ( (Named) ann ).value();
                        if ( !Hints.isDefaultHint( hint ) )
                        {
                            return new LazyBeanEntry<T>( hint, binding.getProvider() );
                        }
                    }
                }
            }
            injectorIndex++;
            bindingIndex = 0;
        }
        return null;
    }
}
