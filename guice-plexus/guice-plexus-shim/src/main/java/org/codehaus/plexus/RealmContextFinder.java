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
package org.codehaus.plexus;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

interface RealmContextFinder
{
    ClassRealm findRealm();
}

final class StackRealmContextFinder
    extends SecurityManager
    implements RealmContextFinder
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String CONTAINER_PACKAGE = "org.codehaus.plexus";

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ClassRealm findRealm()
    {
        for ( final Class<?> clazz : getClassContext() )
        {
            if ( clazz.getName().startsWith( CONTAINER_PACKAGE ) == false )
            {
                final ClassLoader loader = clazz.getClassLoader();
                if ( loader instanceof ClassRealm )
                {
                    return (ClassRealm) loader;
                }
            }
        }
        return null;
    }
}

final class ThreadRealmContextFinder
    extends SecurityManager
    implements RealmContextFinder
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ClassRealm findRealm()
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return tccl instanceof ClassRealm ? (ClassRealm) tccl : null;
    }
}
