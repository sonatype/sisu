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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

public final class ClassRealms
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private ClassRealms()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new ClassRealms(); // keep Cobertura coverage happy
    }

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

    @SuppressWarnings( "unchecked" )
    public static Collection<String> findVisibleRealmIds( final ClassRealm contextRealm )
    {
        final Set<String> visibleRealms = new HashSet<String>();
        final List<ClassRealm> searchRealms = new ArrayList<ClassRealm>();

        searchRealms.add( contextRealm );
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
}
