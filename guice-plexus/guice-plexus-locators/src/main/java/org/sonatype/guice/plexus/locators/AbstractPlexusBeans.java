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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Base {@link PlexusBeans} implementation that manages a backing list of {@link InjectorBeans}.
 */
abstract class AbstractPlexusBeans<T>
    implements PlexusBeans<T>
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

    private List<InjectorBeans<T>> trackedBeans;

    private List<PlexusBeanLocator.Bean<T>> cachedBeans;

    private ClassLoader cachedTCCL;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public final synchronized Iterator<PlexusBeanLocator.Bean<T>> iterator()
    {
        if ( null == cachedBeans || Thread.currentThread().getContextClassLoader() != cachedTCCL )
        {
            cachedTCCL = Thread.currentThread().getContextClassLoader();
            if ( null != trackedBeans )
            {
                cachedBeans = getVisibleBeans( getVisibleRealms( getContextRealm() ) );
            }
            else
            {
                cachedBeans = sequenceBeans( Collections.EMPTY_LIST );
            }
        }
        return cachedBeans.iterator();
    }

    public final synchronized boolean add( final Injector injector )
    {
        final InjectorBeans<T> newBeans = lookupInjectorBeans( injector );
        if ( newBeans.isEmpty() )
        {
            return false; // nothing to add
        }
        if ( null == trackedBeans )
        {
            trackedBeans = new ArrayList<InjectorBeans<T>>( 4 );
        }
        cachedBeans = null;
        return trackedBeans.add( newBeans );
    }

    public final synchronized boolean remove( final Injector injector )
    {
        if ( null == trackedBeans )
        {
            return false; // nothing to remove
        }
        for ( final InjectorBeans<T> beans : trackedBeans )
        {
            if ( injector == beans.injector )
            {
                cachedBeans = null;
                return trackedBeans.remove( beans );
            }
        }
        return false;
    }

    // ----------------------------------------------------------------------
    // Abstract methods
    // ----------------------------------------------------------------------

    abstract InjectorBeans<T> lookupInjectorBeans( final Injector injector );

    abstract List<PlexusBeanLocator.Bean<T>> sequenceBeans( final List<InjectorBeans<T>> beans );

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Last class realm to appear in the {@link Thread#getContextClassLoader()} hierarchy
     */
    private final ClassRealm getContextRealm()
    {
        while ( null != cachedTCCL )
        {
            if ( cachedTCCL instanceof ClassRealm )
            {
                return (ClassRealm) cachedTCCL;
            }
            cachedTCCL = cachedTCCL.getParent();
        }
        return null;
    }

    /**
     * Returns the set of {@link ClassRealm}s that can be seen from the given {@link ClassRealm}.
     * 
     * @param observerRealm Observer class realm
     * @return Set of visible class realms
     */
    @SuppressWarnings( "unchecked" )
    private final static Set<ClassRealm> getVisibleRealms( final ClassRealm observerRealm )
    {
        if ( !GET_IMPORT_REALMS_SUPPORTED || null == observerRealm )
        {
            return Collections.EMPTY_SET;
        }

        final Set<ClassRealm> visibleRealms = new HashSet<ClassRealm>();
        final LinkedList<ClassRealm> searchRealms = new LinkedList<ClassRealm>();

        searchRealms.add( observerRealm );
        while ( !searchRealms.isEmpty() )
        {
            final ClassRealm realm = searchRealms.removeFirst();
            if ( visibleRealms.add( realm ) )
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

    /**
     * Returns the list of Plexus beans that can be seen from the given set of {@link ClassRealm}s.
     * 
     * @param visibleRealms Set of visible class realms
     * @return List of visible Plexus beans
     */
    private final List<PlexusBeanLocator.Bean<T>> getVisibleBeans( final Set<ClassRealm> visibleRealms )
    {
        if ( visibleRealms.isEmpty() )
        {
            return sequenceBeans( trackedBeans );
        }

        final Key<ClassRealm> realmKey = Key.get( ClassRealm.class );
        final List<InjectorBeans<T>> visibleBeans = new ArrayList<InjectorBeans<T>>();
        for ( int i = 0, size = trackedBeans.size(); i < size; i++ )
        {
            final InjectorBeans<T> candidateBeans = trackedBeans.get( i );
            final Binding<ClassRealm> injectorRealm = candidateBeans.injector.getExistingBinding( realmKey );
            if ( null == injectorRealm || visibleRealms.contains( injectorRealm.getProvider().get() ) )
            {
                visibleBeans.add( candidateBeans );
            }
        }

        return sequenceBeans( visibleBeans );
    }
}
