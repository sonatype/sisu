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
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.guice.bean.locators.QualifiedBean;
import org.sonatype.guice.plexus.config.PlexusBean;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

final class RealmPlexusBeans<T>
    extends GlobalPlexusBeans<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private ClassRealm cachedContextRealm;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RealmPlexusBeans( final TypeLiteral<T> role, final String... hints )
    {
        super( role, hints );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<PlexusBean<T>> iterator()
    {
        synchronized ( beans )
        {
            final ClassRealm contextRealm = ClassRealmUtils.contextRealm();
            if ( null == cachedBeans || contextRealm != cachedContextRealm )
            {
                cachedContextRealm = contextRealm;
                cachedBeans = getPlexusBeans( getVisibleBeans( contextRealm ), defaultPlexusBeans );
            }
            return cachedBeans.iterator();
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Iterable<QualifiedBean<Named, T>> getVisibleBeans( final ClassRealm contextRealm )
    {
        final Set<String> visibleRealmNames = ClassRealmUtils.visibleRealmNames( contextRealm );
        if ( visibleRealmNames.isEmpty() )
        {
            return beans;
        }
        final List<QualifiedBean<Named, T>> visibleBeans = new ArrayList<QualifiedBean<Named, T>>();
        for ( final QualifiedBean<Named, T> bean : beans )
        {
            final String source = bean.getBinding().getSource().toString();
            if ( !source.startsWith( "ClassRealm" ) || visibleRealmNames.contains( source ) )
            {
                visibleBeans.add( bean );
            }
        }
        return visibleBeans;
    }
}
