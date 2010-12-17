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

    private final List<PlexusBean<T>> missingPlexusBeans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HintedPlexusBeans( final Iterable<QualifiedBean<Named, T>> beans, final TypeLiteral<T> role, final String[] hints )
    {
        this.beans = beans;

        missingPlexusBeans = new ArrayList<PlexusBean<T>>( hints.length );
        for ( final String h : hints )
        {
            missingPlexusBeans.add( new MissingPlexusBean<T>( role, h ) );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<PlexusBean<T>> iterator()
    {
        final List<PlexusBean<T>> plexusBeans = new ArrayList<PlexusBean<T>>( missingPlexusBeans );

        final int size = plexusBeans.size();
        final Iterator<QualifiedBean<Named, T>> itr = beans.iterator();
        for ( int numFound = 0; numFound < size && itr.hasNext(); )
        {
            final QualifiedBean<Named, T> candidate = itr.next();
            final String hint = candidate.getKey().value();
            for ( int i = 0; i < size; i++ )
            {
                final PlexusBean<T> element = plexusBeans.get( i );
                if ( element instanceof MissingPlexusBean<?> && hint.equals( element.getKey() ) )
                {
                    plexusBeans.set( i, new LazyPlexusBean<T>( candidate ) );
                    numFound++;
                }
            }
        }

        return plexusBeans.iterator();
    }
}
