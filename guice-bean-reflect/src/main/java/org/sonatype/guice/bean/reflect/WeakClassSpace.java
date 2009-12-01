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
package org.sonatype.guice.bean.reflect;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;

/**
 * {@link ClassSpace} backed by a weakly-referenced {@link ClassLoader}.
 */
public final class WeakClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private final static String CLASS_SPACE_UNLOADED = "ClassSpace has been unloaded.";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Reference<ClassLoader> classLoaderRef;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public WeakClassSpace( final ClassLoader classLoader )
    {
        classLoaderRef = new WeakReference<ClassLoader>( classLoader );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> loadClass( final String name )
        throws ClassNotFoundException
    {
        final ClassLoader classLoader = classLoaderRef.get();
        if ( null != classLoader )
        {
            return classLoader.loadClass( name );
        }
        throw new ClassNotFoundException( CLASS_SPACE_UNLOADED );
    }

    public Enumeration<URL> getResources( final String name )
        throws IOException
    {
        final ClassLoader classLoader = classLoaderRef.get();
        if ( null != classLoader )
        {
            return classLoader.getResources( name );
        }
        throw new IOException( CLASS_SPACE_UNLOADED );
    }
}