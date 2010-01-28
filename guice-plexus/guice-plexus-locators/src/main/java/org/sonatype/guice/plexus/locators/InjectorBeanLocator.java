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

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * {@link PlexusBeanLocator} that locates @{@link Named} beans based on explicit {@link Injector} bindings.
 */
public final class InjectorBeanLocator
    implements PlexusBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    public InjectorBeanLocator( final Injector injector )
    {
        this.injector = injector;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> Iterable<Entry<String, T>> locate( final TypeLiteral<T> type, final String... hints )
    {
        return hints.length == 0 ? locateByType( type ) : locateByName( type, hints );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Locates all beans of the given type; the default bean will be first, if it exists.
     * 
     * @param type The expected bean type
     * @return Sequence of matching beans
     */
    private <T> Iterable<Entry<String, T>> locateByType( final TypeLiteral<T> type )
    {
        final List<Entry<String, T>> entries = new ArrayList<Entry<String, T>>();
        for ( final Binding<T> binding : injector.findBindingsByType( type ) )
        {
            final Annotation ann = binding.getKey().getAnnotation();
            if ( null == ann )
            {
                entries.add( 0, new LazyBeanEntry<T>( Hints.DEFAULT_HINT, binding.getProvider() ) );
            }
            else if ( ann instanceof Named )
            {
                final String hint = ( (Named) ann ).value();
                if ( !Hints.isDefaultHint( hint ) )
                {
                    entries.add( new LazyBeanEntry<T>( hint, binding.getProvider() ) );
                }
            }
        }
        return entries;
    }

    /**
     * Locates named beans of the given type; ordered according to the given name sequence.
     * 
     * @param type The expected bean type
     * @param hints The expected name sequence
     * @return Sequence of matching beans; ordered according to the given name sequence.
     */
    @SuppressWarnings( "unchecked" )
    private <T> Iterable<Entry<String, T>> locateByName( final TypeLiteral<T> type, final String... hints )
    {
        final List<Entry<String, T>> entries = new ArrayList<Entry<String, T>>();
        final Map<Key<?>, Binding<?>> bindings = injector.getBindings();
        for ( final String h : hints )
        {
            // we already know what names to expect, but some might be missing
            final Binding binding = bindings.get( Roles.componentKey( type, h ) );
            if ( binding != null )
            {
                entries.add( new LazyBeanEntry<T>( h, binding.getProvider() ) );
            }
            else
            {
                entries.add( new MissingBeanEntry<T>( type, h ) );
            }
        }
        return entries;
    }
}
