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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.guice.bean.locators.QualifiedBean;
import org.sonatype.guice.plexus.config.PlexusBean;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

final class PlexusBeans<T>
    implements Iterable<PlexusBean<T>>, Runnable
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean GET_IMPORT_REALMS_SUPPORTED;

    static
    {
        boolean getImportRealmsSupported = true;
        try
        {
            // support both old and new forms of Plexus class realms
            ClassRealm.class.getDeclaredMethod( "getImportRealms" );
        }
        catch ( final Throwable e )
        {
            getImportRealmsSupported = false;
        }
        GET_IMPORT_REALMS_SUPPORTED = getImportRealmsSupported;
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<PlexusBean<T>> defaultPlexusBeans;

    private Iterable<QualifiedBean<Named, T>> beans;

    private Iterable<PlexusBean<T>> cachedBeans;

    private ClassLoader cachedTCCL;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBeans( final TypeLiteral<T> role, final String... hints )
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

    public void setBeans( final Iterable<QualifiedBean<Named, T>> beans )
    {
        this.beans = beans;
    }

    public synchronized Iterator<PlexusBean<T>> iterator()
    {
        final ClassLoader currentTCCL = Thread.currentThread().getContextClassLoader();
        if ( null == cachedBeans || currentTCCL != cachedTCCL )
        {
            cachedTCCL = currentTCCL;
            final Set<String> visibleRealms = getVisibleRealms( getContextRealm( currentTCCL ) );
            cachedBeans = getPlexusBeans( getVisibleBeans( visibleRealms ), defaultPlexusBeans );
        }
        return cachedBeans.iterator();
    }

    public synchronized void run()
    {
        cachedBeans = null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Last class realm to appear in the {@link Thread#getContextClassLoader()} hierarchy
     */
    private static ClassRealm getContextRealm( final ClassLoader currentTCCL )
    {
        ClassLoader tccl = currentTCCL;
        while ( null != tccl )
        {
            if ( tccl instanceof ClassRealm )
            {
                return (ClassRealm) tccl;
            }
            tccl = tccl.getParent();
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    private static Set<String> getVisibleRealms( final ClassRealm observerRealm )
    {
        if ( !GET_IMPORT_REALMS_SUPPORTED || null == observerRealm )
        {
            return Collections.EMPTY_SET;
        }

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

    private Iterable<QualifiedBean<Named, T>> getVisibleBeans( final Set<String> visibleRealms )
    {
        if ( visibleRealms.isEmpty() )
        {
            return beans;
        }
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

    private static <T> Iterable<PlexusBean<T>> getPlexusBeans( final Iterable<QualifiedBean<Named, T>> beans,
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
        final List<PlexusBean<T>> plexusBeans = new ArrayList<PlexusBean<T>>( defaultPlexusBeans );
        while ( i.hasNext() )
        {
            final QualifiedBean<Named, T> bean = i.next();
            for ( int n = 0; n < expectedSize; n++ )
            {
                if ( bean.getKey().value().equals( plexusBeans.get( n ).getKey() ) )
                {
                    plexusBeans.set( n, new LazyPlexusBean<T>( bean ) );
                }
            }
        }
        return plexusBeans;
    }
}
