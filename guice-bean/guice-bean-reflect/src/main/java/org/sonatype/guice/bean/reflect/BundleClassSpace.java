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
    {
        try
        {
            return bundle.loadClass( name );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new TypeNotPresentException( name, e );
        }
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
    {
        try
        {
            return bundle.getResources( name );
        }
        catch ( final IOException e )
        {
            return ResourceEnumeration.NO_RESOURCES;
        }
    }

    @SuppressWarnings( "unchecked" )
    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        final Enumeration e = bundle.findEntries( null != path ? path : "/", glob, recurse );
        return null != e ? e : EMPTY_ENUMERATION;
    }

    @Override
    public int hashCode()
    {
        return bundle.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof BundleClassSpace )
        {
            return bundle.equals( ( (BundleClassSpace) rhs ).bundle );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return bundle.toString();
    }
}