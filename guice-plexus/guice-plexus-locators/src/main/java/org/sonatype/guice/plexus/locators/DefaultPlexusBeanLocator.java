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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.plexus.config.PlexusBean;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * {@link PlexusBeanLocator} that locates beans of various types from zero or more {@link Injector}s.
 */
@Singleton
public final class DefaultPlexusBeanLocator
    implements PlexusBeanLocator
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String REALM_VISIBILITY = "realm";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanLocator beanLocator;

    private final String visibility;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    public DefaultPlexusBeanLocator( final BeanLocator beanLocator )
    {
        this( beanLocator, REALM_VISIBILITY );
    }

    public DefaultPlexusBeanLocator( final BeanLocator beanLocator, final String visibility )
    {
        this.beanLocator = beanLocator;
        this.visibility = visibility;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> Iterable<PlexusBean<T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        final PlexusBeans<T> plexusBeans;
        if ( REALM_VISIBILITY.equalsIgnoreCase( visibility ) )
        {
            plexusBeans = new RealmPlexusBeans<T>( role, hints );
        }
        else
        {
            plexusBeans = new GlobalPlexusBeans<T>( role, hints );
        }
        if ( hints.length == 1 )
        {
            plexusBeans.setBeans( beanLocator.<Named, T> locate( Key.get( role, Names.named( hints[0] ) ), plexusBeans ) );
        }
        else
        {
            plexusBeans.setBeans( beanLocator.<Named, T> locate( Key.get( role, Named.class ), plexusBeans ) );
        }
        return plexusBeans;
    }
}