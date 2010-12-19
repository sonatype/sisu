/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

public final class ClassRealmUtils
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean GET_IMPORT_REALMS_SUPPORTED;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private ClassRealmUtils()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new ClassRealmUtils(); // keep Cobertura coverage happy

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

    private static Map<ClassRealm, RealmInfo> realmCache = new WeakHashMap<ClassRealm, RealmInfo>();

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static ClassRealm contextRealm()
    {
        for ( ClassLoader tccl = Thread.currentThread().getContextClassLoader(); tccl != null; tccl = tccl.getParent() )
        {
            if ( tccl instanceof ClassRealm )
            {
                return (ClassRealm) tccl;
            }
        }
        return null;
    }

    public static Set<String> visibleRealmNames( final ClassRealm contextRealm )
    {
        if ( false == GET_IMPORT_REALMS_SUPPORTED || null == contextRealm )
        {
            return Collections.emptySet();
        }

        synchronized ( realmCache )
        {
            RealmInfo realmInfo = realmCache.get( contextRealm );
            if ( null == realmInfo )
            {
                realmInfo = new RealmInfo( contextRealm );
                realmCache.put( contextRealm, realmInfo );
            }
            return realmInfo.getVisibleRealmNames();
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class RealmInfo
    {
        private final Set<String> visibleRealmNames = new HashSet<String>();

        @SuppressWarnings( "unchecked" )
        RealmInfo( final ClassRealm forRealm )
        {
            final List<ClassRealm> searchRealms = new ArrayList<ClassRealm>();

            searchRealms.add( forRealm );
            for ( int i = 0; i < searchRealms.size(); i++ )
            {
                final ClassRealm realm = searchRealms.get( i );
                if ( visibleRealmNames.add( realm.toString() ) )
                {
                    searchRealms.addAll( realm.getImportRealms() );
                    final ClassRealm parent = realm.getParentRealm();
                    if ( null != parent )
                    {
                        searchRealms.add( parent );
                    }
                }
            }
        }

        public Set<String> getVisibleRealmNames()
        {
            return visibleRealmNames;
        }
    }
}
