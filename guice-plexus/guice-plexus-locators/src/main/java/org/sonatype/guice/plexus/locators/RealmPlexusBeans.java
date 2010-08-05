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
import java.util.HashSet;
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

    private ClassLoader cachedTCCL;

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
    public synchronized Iterator<PlexusBean<T>> iterator()
    {
        final ClassLoader currentTCCL = Thread.currentThread().getContextClassLoader();
        if ( null == cachedBeans || currentTCCL != cachedTCCL )
        {
            cachedTCCL = currentTCCL;
            cachedBeans = getPlexusBeans( getVisibleBeans( getContextRealm( currentTCCL ) ), defaultPlexusBeans );
        }
        return cachedBeans.iterator();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Last class realm to appear in the {@link Thread#getContextClassLoader()} hierarchy
     */
    private static ClassRealm getContextRealm( final ClassLoader currentTCCL )
    {
        for ( ClassLoader tccl = currentTCCL; tccl != null; tccl = tccl.getParent() )
        {
            if ( tccl instanceof ClassRealm )
            {
                return (ClassRealm) tccl;
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    private static Set<String> getVisibleRealms( final ClassRealm observerRealm )
    {
        final Set<String> visibleRealms = new HashSet<String>();
        final List<ClassRealm> searchRealms = new ArrayList<ClassRealm>();

        searchRealms.add( observerRealm );
        for ( int i = 0; i < searchRealms.size(); i++ )
        {
            final ClassRealm realm = searchRealms.get( i );
            if ( visibleRealms.add( realm.toString() ) )
            {
                searchRealms.addAll( realm.getImportRealms() );
                final ClassRealm parent = realm.getParentRealm();
                if ( null != parent )
                {
                    searchRealms.add( parent );
                }
            }
        }

        return visibleRealms;
    }

    private Iterable<QualifiedBean<Named, T>> getVisibleBeans( final ClassRealm observerRealm )
    {
        if ( null == observerRealm )
        {
            return beans;
        }
        final Set<String> visibleRealms = getVisibleRealms( observerRealm );
        final List<QualifiedBean<Named, T>> visibleBeans = new ArrayList<QualifiedBean<Named, T>>();
        for ( final QualifiedBean<Named, T> bean : beans )
        {
            final String source = bean.getBinding().getSource().toString();
            if ( !source.startsWith( "ClassRealm" ) || visibleRealms.contains( source ) )
            {
                visibleBeans.add( bean );
            }
        }
        return visibleBeans;
    }
}
