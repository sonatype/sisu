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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sonatype.guice.bean.locators.QualifiedBean;
import org.sonatype.guice.plexus.config.PlexusBean;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

class GlobalPlexusBeans<T>
    implements PlexusBeans<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final List<PlexusBean<T>> defaultPlexusBeans;

    Iterable<QualifiedBean<Named, T>> beans;

    Iterable<PlexusBean<T>> cachedBeans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    GlobalPlexusBeans( final TypeLiteral<T> role, final String... hints )
    {
        if ( hints.length > 0 )
        {
            defaultPlexusBeans = new ArrayList<PlexusBean<T>>( hints.length );
            for ( final String hint : hints )
            {
                defaultPlexusBeans.add( new MissingPlexusBean<T>( role, hint ) );
            }
        }
        else
        {
            defaultPlexusBeans = Collections.emptyList();
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setBeans( final Iterable<QualifiedBean<Named, T>> beans )
    {
        this.beans = beans;
    }

    public Iterator<PlexusBean<T>> iterator()
    {
        synchronized ( beans )
        {
            if ( null == cachedBeans )
            {
                cachedBeans = getPlexusBeans( beans, defaultPlexusBeans );
            }
            return cachedBeans.iterator();
        }
    }

    public final void run()
    {
        synchronized ( beans )
        {
            cachedBeans = null;
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    static final <T> Iterable<PlexusBean<T>> getPlexusBeans( final Iterable<QualifiedBean<Named, T>> beans,
                                                             final List<PlexusBean<T>> defaultPlexusBeans )
    {
        final Iterator<QualifiedBean<Named, T>> i = beans.iterator();
        if ( !i.hasNext() )
        {
            return defaultPlexusBeans;
        }
        final int expectedSize = defaultPlexusBeans.size();
        if ( expectedSize == 0 )
        {
            final List<PlexusBean<T>> plexusBeans = new ArrayList<PlexusBean<T>>();
            while ( i.hasNext() )
            {
                plexusBeans.add( new LazyPlexusBean<T>( i.next() ) );
            }
            return plexusBeans;
        }
        int beanCount = 0;
        final List<PlexusBean<T>> plexusBeans = new ArrayList<PlexusBean<T>>( defaultPlexusBeans );
        while ( i.hasNext() )
        {
            final QualifiedBean<Named, T> bean = i.next();
            final String key = bean.getKey().value();
            for ( int n = 0; n < expectedSize; n++ )
            {
                final PlexusBean<T> b = plexusBeans.get( n );
                if ( key.equals( b.getKey() ) && b instanceof MissingPlexusBean<?> )
                {
                    plexusBeans.set( n, new LazyPlexusBean<T>( bean ) );
                    if ( ++beanCount == expectedSize )
                    {
                        return plexusBeans;
                    }
                }
            }
        }
        return plexusBeans;
    }
}
