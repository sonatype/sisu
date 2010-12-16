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

import java.util.Arrays;
import java.util.Iterator;

import org.sonatype.guice.bean.locators.QualifiedBean;
import org.sonatype.guice.plexus.config.PlexusBean;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

final class HintedPlexusBeans<T>
    implements Iterable<PlexusBean<T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<QualifiedBean<Named, T>> beans;

    private final TypeLiteral<T> role;

    private final String[] hints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HintedPlexusBeans( final Iterable<QualifiedBean<Named, T>> beans, final TypeLiteral<T> role, final String[] hints )
    {
        this.beans = beans;
        this.role = role;
        this.hints = hints;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<PlexusBean<T>> iterator()
    {
        @SuppressWarnings( "unchecked" )
        final PlexusBean<T>[] plexusBeans = new PlexusBean[hints.length];

        int beanCount = 0;
        final Iterator<QualifiedBean<Named, T>> itr = beans.iterator();
        while ( beanCount < hints.length && itr.hasNext() )
        {
            final QualifiedBean<Named, T> bean = itr.next();
            final String hint = bean.getKey().value();
            for ( int i = 0; i < hints.length; i++ )
            {
                if ( hints[i].equals( hint ) && plexusBeans[i] == null )
                {
                    plexusBeans[i] = new LazyPlexusBean<T>( bean );
                    beanCount++;
                }
            }
        }

        for ( int i = 0; i < hints.length; i++ )
        {
            if ( plexusBeans[i] == null )
            {
                plexusBeans[i] = new MissingPlexusBean<T>( role, hints[i] );
            }
        }

        return Arrays.asList( plexusBeans ).iterator();
    }
}