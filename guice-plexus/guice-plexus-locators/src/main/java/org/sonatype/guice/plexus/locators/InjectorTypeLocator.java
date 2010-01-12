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
import org.sonatype.guice.plexus.config.PlexusTypeLocator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

final class InjectorTypeLocator
    implements PlexusTypeLocator
{
    private final Injector injector;

    public InjectorTypeLocator( final Injector injector )
    {
        this.injector = injector;
    }

    public <T> Iterable<Entry<String, T>> locate( final TypeLiteral<T> type, final String... hints )
    {
        return hints.length == 0 ? locateByType( type ) : locateByName( type, hints );
    }

    private <T> Iterable<Entry<String, T>> locateByType( final TypeLiteral<T> type )
    {
        final List<Entry<String, T>> entries = new ArrayList<Entry<String, T>>();
        for ( final Binding<T> binding : injector.findBindingsByType( type ) )
        {
            final Annotation ann = binding.getKey().getAnnotation();
            if ( ann instanceof Named && !Hints.isDefaultHint( ( (Named) ann ).value() ) )
            {
                entries.add( new BindingEntry<T>( binding ) );
            }
            else if ( null == ann )
            {
                entries.add( 0, new BindingEntry<T>( binding ) );
            }
        }
        return entries;
    }

    @SuppressWarnings( "unchecked" )
    private <T> Iterable<Entry<String, T>> locateByName( final TypeLiteral<T> type, final String[] hints )
    {
        final List<Entry<String, T>> entries = new ArrayList<Entry<String, T>>();
        final Map<Key<?>, Binding<?>> bindings = injector.getBindings();
        for ( final String h : hints )
        {
            final Binding binding = bindings.get( Roles.componentKey( type, h ) );
            if ( binding != null )
            {
                entries.add( new BindingEntry( binding ) );
            }
            else
            {
                entries.add( new MissingEntry( type, h ) );
            }
        }
        return entries;
    }

    private static final class BindingEntry<T>
        implements Entry<String, T>
    {
        private final String hint;

        private Provider<T> provider;

        private T value;

        BindingEntry( final Binding<T> binding )
        {
            final Annotation a = binding.getKey().getAnnotation();
            hint = a instanceof Named ? ( (Named) a ).value() : Hints.DEFAULT_HINT;
            provider = binding.getProvider();
        }

        public String getKey()
        {
            return hint;
        }

        public synchronized T getValue()
        {
            if ( null == value )
            {
                value = provider.get();
                provider = null;
            }
            return value;
        }

        public T setValue( final T value )
        {
            throw new UnsupportedOperationException();
        }
    }

    private static final class MissingEntry<T>
        implements Entry<String, T>
    {
        private final TypeLiteral<T> type;

        private final String hint;

        MissingEntry( final TypeLiteral<T> type, final String hint )
        {
            this.type = type;
            this.hint = hint;
        }

        public String getKey()
        {
            return hint;
        }

        public T getValue()
        {
            return Roles.throwMissingComponentException( type, hint );
        }

        public T setValue( final T value )
        {
            throw new UnsupportedOperationException();
        }
    }
}
