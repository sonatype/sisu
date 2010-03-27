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
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

/**
 * {@link ClassSpace} backed by a strongly-referenced {@link Bundle}.
 */
public final class BundleClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private static final Enumeration EMPTY_ENUMERATION = Collections.enumeration( Collections.EMPTY_LIST );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Bundle bundle;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BundleClassSpace( final Bundle bundle )
    {
        this.bundle = bundle;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> loadClass( final String name )
        throws ClassNotFoundException
    {
        return bundle.loadClass( name );
    }

    public DeferredClass<?> deferLoadClass( final String name )
    {
        return new StrongDeferredClass<Object>( this, name );
    }

    public URL getResource( final String name )
    {
        return bundle.getResource( name );
    }

    @SuppressWarnings( "unchecked" )
    public Enumeration<URL> getResources( final String name )
        throws IOException
    {
        return bundle.getResources( name );
    }

    @SuppressWarnings( "unchecked" )
    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        final Enumeration e = bundle.findEntries( path, glob, recurse );
        return null != e ? e : EMPTY_ENUMERATION;
    }
}