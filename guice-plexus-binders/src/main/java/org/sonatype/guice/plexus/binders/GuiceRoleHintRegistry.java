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
import java.util.AbstractList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.guice.plexus.config.Hints;

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
 * Registry that queries the Guice {@link Injector} to find role-hint {@link Binding}s.
 */
@Singleton
final class GuiceRoleHintRegistry<T>
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

    final DeferredInjector deferredInjector;

    private final String roleName;

    private final Map<String, Provider<T>> roleHintMap;

    private final String[] allHints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    GuiceRoleHintRegistry( final Injector injector, final TypeLiteral<T> roleType )
    {
        this.deferredInjector = injector.getInstance( DeferredInjector.class );
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

        if ( !tempMap.isEmpty() )
        {
            // ordering is recorded in hint array, so can use simpler hash map
            allHints = tempMap.keySet().toArray( new String[tempMap.size()] );
            roleHintMap = new HashMap<String, Provider<T>>( tempMap );
        }
        else
        {
            allHints = Hints.NO_HINTS;
            roleHintMap = Collections.emptyMap();
        }
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    Iterable<Entry<String, T>> locate( final String... canonicalHints )
    {
        if ( canonicalHints.length == 0 )
        {
            if ( allHints.length == 0 )
            {
                return Collections.emptyList();
            }
            return new ProvidedEntryList( allHints );
        }
        return new ProvidedEntryList( canonicalHints );
    }

    // ----------------------------------------------------------------------
    // Implementation classes
    // ----------------------------------------------------------------------

    private final class ProvidedEntryList
        extends AbstractList<Entry<String, T>>
    {
        private static final long serialVersionUID = 1L;

        private final String[] hints;

        private final Entry<String, T>[] elements;

        @SuppressWarnings( "unchecked" )
        ProvidedEntryList( final String[] hints )
        {
            this.hints = hints;

            elements = new Entry[hints.length];
        }

        @Override
        public synchronized Entry<String, T> get( final int index )
        {
            if ( null == elements[index] )
            {
                elements[index] = new ProvidedEntry( hints[index] );
            }
            return elements[index];
        }

        @Override
        public int size()
        {
            return hints.length;
        }
    }

    private final class ProvidedEntry
        implements Entry<String, T>
    {
        private final String hint;

        private T value;

        ProvidedEntry( final String hint )
        {
            this.hint = hint;
        }

        public String getKey()
        {
            return hint;
        }

        public synchronized T getValue()
        {
            if ( null == value )
            {
                value = lookupRole( hint );
                deferredInjector.resume();
            }
            return value;
        }

        public T setValue( final T value )
        {
            throw new UnsupportedOperationException();
        }
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
    T lookupRole( final String canonicalHint )
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