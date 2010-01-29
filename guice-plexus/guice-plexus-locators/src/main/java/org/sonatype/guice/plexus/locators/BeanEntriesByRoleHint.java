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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import javax.inject.Provider;

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
    implements Iterable<Entry<String, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector injector;

    private final TypeLiteral<T> role;

    private final String[] hints;

    private List<Entry<String, T>> cache;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanEntriesByRoleHint( final Injector injector, final TypeLiteral<T> role, final String... hints )
    {
        this.injector = injector;
        this.role = role;
        this.hints = hints;
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
        else if ( index >= hints.length )
        {
            return false;
        }
        Entry<String, T> roleHint = lookupRoleHint( injector, role, hints[index] );
        if ( null == roleHint )
        {
            roleHint = new MissingBeanEntry<T>( role, hints[index] );
        }
        return cache.add( roleHint );
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

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    static <T> Entry<String, T> lookupRoleHint( final Injector injector, final TypeLiteral<T> role, final String hint )
    {
        final Key key = Roles.componentKey( role, hint );
        if ( null == injector.getParent() )
        {
            try
            {
                final Provider provider = injector.getProvider( key );
                return new LazyBeanEntry( hint, provider );
            }
            catch ( final ConfigurationException e )
            {
                return null;
            }
        }

        final Binding binding = injector.getBindings().get( key );
        if ( null != binding )
        {
            final Provider provider = binding.getProvider();
            return new LazyBeanEntry( hint, provider );
        }

        return null;
    }
}
