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
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

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

    private static Map<ClassRealm, Set<String>> visibleRealmNameCache =
        new MapMaker().weakKeys().makeComputingMap( new VisibleNameFunction() );

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
        return GET_IMPORT_REALMS_SUPPORTED && null != contextRealm ? visibleRealmNameCache.get( contextRealm ) : null;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    static final class VisibleNameFunction
        implements Function<ClassRealm, Set<String>>
    {
        @SuppressWarnings( "unchecked" )
        public Set<String> apply( final ClassRealm forRealm )
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
