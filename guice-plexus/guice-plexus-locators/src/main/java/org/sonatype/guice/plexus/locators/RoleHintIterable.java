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
final class RoleHintIterable<T>
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

    RoleHintIterable( final Injector injector, final TypeLiteral<T> role, final String... hints )
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

    @SuppressWarnings( "unchecked" )
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

        final String hint = hints[index];
        final Key key = Roles.componentKey( role, hint );

        if ( null == injector.getParent() )
        {
            try
            {
                final Provider<T> provider = injector.getProvider( key );
                cache.add( new LazyBeanEntry( hint, provider ) );
            }
            catch ( final ConfigurationException e )
            {
                cache.add( new MissingBeanEntry( role, hint ) );
            }
        }
        else
        {
            final Binding binding = injector.getBindings().get( key );
            if ( null != binding )
            {
                final Provider<T> provider = binding.getProvider();
                cache.add( new LazyBeanEntry( hint, provider ) );
            }
            else
            {
                cache.add( new MissingBeanEntry( role, hint ) );
            }
        }

        return true;
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
