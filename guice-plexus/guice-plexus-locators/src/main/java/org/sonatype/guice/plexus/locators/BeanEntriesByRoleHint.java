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

import java.util.List;
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
final class BeanEntriesByRoleHint<T>
    extends AbstractBeanEntries<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String[] hints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanEntriesByRoleHint( final List<Injector> injectors, final TypeLiteral<T> role, final String[] hints )
    {
        super( injectors, role );
        this.hints = hints;
    }

    // ----------------------------------------------------------------------
    // Cache methods
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings( "unchecked" )
    Entry<String, T> populate( final int index )
    {
        if ( index >= hints.length )
        {
            return null;
        }
        final String hint = hints[index];
        final Key key = Roles.componentKey( role, hint );
        try
        {
            return new LazyBeanEntry( hint, injectors.get( 0 ).getProvider( key ) );
        }
        catch ( final ConfigurationException e ) // NOPMD
        {
            // drop-through and search child injectors
        }
        for ( int i = 1; i < injectors.size(); i++ )
        {
            final Injector injector = injectors.get( i );
            if ( null != injector )
            {
                final Binding binding = injector.getBindings().get( key );
                if ( null != binding )
                {
                    return new LazyBeanEntry( hint, binding.getProvider() );
                }
            }
        }
        return new MissingBeanEntry( role, hint );
    }
}
