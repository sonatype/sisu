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

import static org.sonatype.guice.plexus.locators.RoleHintIterable.lookupRoleHint;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.guice.plexus.config.Hints;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * 
 */
final class RoleIterable<T>
    implements Iterable<Entry<String, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector injector;

    private final TypeLiteral<T> role;

    private int bindingIndex;

    private List<Entry<String, T>> cache;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RoleIterable( final Injector injector, final TypeLiteral<T> role )
    {
        this.injector = injector;
        this.role = role;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<Entry<String, T>> iterator()
    {
        return new CachingIterator();
    }

    // ----------------------------------------------------------------------
    // Cache methods
    // ----------------------------------------------------------------------

    synchronized boolean populate( final int index )
    {
        if ( null == cache )
        {
            cache = new ArrayList<Entry<String, T>>();
        }
        else if ( index < cache.size() )
        {
            return true;
        }

        if ( 0 == index )
        {
            final Entry<String, T> defaultRoleHint = lookupRoleHint( injector, role, Hints.DEFAULT_HINT );
            if ( null != defaultRoleHint )
            {
                return cache.add( defaultRoleHint );
            }
        }

        final List<Binding<T>> bindings = injector.findBindingsByType( role );
        while ( bindingIndex < bindings.size() )
        {
            final Binding<T> binding = bindings.get( bindingIndex++ );
            final Annotation ann = binding.getKey().getAnnotation();
            if ( ann instanceof Named )
            {
                final String hint = ( (Named) ann ).value();
                if ( !Hints.isDefaultHint( hint ) )
                {
                    final Provider<T> provider = binding.getProvider();
                    cache.add( new LazyRoleHint<T>( hint, provider ) );
                    return true;
                }
            }
        }

        return false;
    }

    synchronized Entry<String, T> lookup( final int index )
    {
        return cache.get( index );
    }

    // ----------------------------------------------------------------------
    // Iterator implementation
    // ----------------------------------------------------------------------

    final class CachingIterator
        implements Iterator<Entry<String, T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return populate( index );
        }

        public Entry<String, T> next()
        {
            if ( hasNext() )
            {
                return lookup( index++ );
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
