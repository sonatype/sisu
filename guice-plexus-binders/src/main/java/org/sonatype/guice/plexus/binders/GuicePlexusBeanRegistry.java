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
package org.sonatype.guice.plexus.binders;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanRegistry;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

/**
 * {@link PlexusBeanRegistry} that queries the Guice {@link Injector} to find role {@link Binding}s.
 */
@Singleton
final class GuicePlexusBeanRegistry<T>
    implements PlexusBeanRegistry<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String MISSING_ROLE_ERROR = "No implementation for %s was bound.";

    private static final String MISSING_ROLE_HINT_ERROR =
        "No implementation for %s annotated with @com.google.inject.name.Named(value=%s) was bound.";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String roleName;

    private final Map<String, Provider<T>> roleHintMap;

    private final String[] allHints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    GuicePlexusBeanRegistry( final Injector injector, final TypeLiteral<T> roleType )
    {
        this.roleName = roleType.toString();

        // find all known bindings for the role, note: excludes Just-In-Time bindings!
        final List<Binding<T>> typeBindings = injector.findBindingsByType( roleType );
        final int numBindings = typeBindings.size();

        final Map<String, Provider<T>> tempMap = new LinkedHashMap<String, Provider<T>>( 2 * numBindings );

        try
        {
            // use explicit query for default, in case it's a Just-In-Time binding
            tempMap.put( Hints.DEFAULT_HINT, injector.getProvider( Key.get( roleType ) ) );
        }
        catch ( final ConfigurationException e ) // NOPMD
        {
            // safe to ignore, as a default component may not always be available
        }

        // @Named bindings => Plexus hints
        for ( int i = 0; i < numBindings; i++ )
        {
            final Binding<T> b = typeBindings.get( i );
            final Annotation a = b.getKey().getAnnotation();
            if ( a instanceof Named )
            {
                // ignore default bindings as we already captured that above
                final String hint = Hints.canonicalHint( ( (Named) a ).value() );
                if ( !Hints.isDefaultHint( hint ) )
                {
                    tempMap.put( hint, b.getProvider() );
                }
            }
        }

        // ordering is recorded in hint array, so can use simpler hash map
        allHints = tempMap.keySet().toArray( new String[tempMap.size()] );
        roleHintMap = new HashMap<String, Provider<T>>( tempMap );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public List<String> availableHints()
    {
        return Arrays.asList( allHints );
    }

    public List<T> lookupList( final String... canonicalHints )
    {
        final List<T> componentList = new ArrayList<T>();
        for ( final String h : canonicalHints.length == 0 ? allHints : canonicalHints )
        {
            componentList.add( lookupRole( h ) );
        }
        return componentList;
    }

    public Map<String, T> lookupMap( final String... canonicalHints )
    {
        final Map<String, T> componentMap = new LinkedHashMap<String, T>();
        for ( final String h : canonicalHints.length == 0 ? allHints : canonicalHints )
        {
            componentMap.put( h, lookupRole( h ) );
        }
        return componentMap;
    }

    public T lookupWildcard()
    {
        if ( allHints.length == 0 )
        {
            throw new ProvisionException( String.format( MISSING_ROLE_ERROR, roleName ) );
        }
        return lookupRole( allHints[0] );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns the component instance that matches the given Plexus hint.
     * 
     * @param canonicalHint The Plexus hint
     * @return Component instance that matches the given hint
     */
    private T lookupRole( final String canonicalHint )
    {
        final Provider<T> provider = roleHintMap.get( canonicalHint );
        if ( null == provider )
        {
            if ( Hints.isDefaultHint( canonicalHint ) )
            {
                throw new ProvisionException( String.format( MISSING_ROLE_ERROR, roleName ) );
            }
            throw new ProvisionException( String.format( MISSING_ROLE_HINT_ERROR, roleName, canonicalHint ) );
        }
        return provider.get();
    }
}