/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.locators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public final class ClassRealmUtils
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static Cache<ClassRealm, Set<String>> visibleRealmNameCache =
        CacheBuilder.newBuilder().weakKeys().build( new VisibleRealmNames() );

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
        if ( GET_IMPORT_REALMS_SUPPORTED && null != contextRealm )
        {
            return visibleRealmNameCache.getUnchecked( contextRealm );
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    static final class VisibleRealmNames
        extends CacheLoader<ClassRealm, Set<String>>
    {
        @Override
        @SuppressWarnings( "unchecked" )
        public Set<String> load( final ClassRealm forRealm )
        {
            final Set<String> visibleRealmNames = new HashSet<String>();
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
            return visibleRealmNames;
        }
    }
}
