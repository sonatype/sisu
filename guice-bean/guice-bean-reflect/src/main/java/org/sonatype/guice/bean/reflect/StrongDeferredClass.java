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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

/**
 * Strong {@link DeferredClass} representing a named class from a {@link ClassSpace}.
 */
public final class StrongDeferredClass<T>
    implements DeferredClass<T>, DeferredProvider<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger( StrongDeferredClass.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final String name;

    @Inject
    private Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public StrongDeferredClass( final ClassSpace space, final String name )
    {
        this.space = space;
        this.name = name;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public Class<T> load()
    {
        try
        {
            return (Class<T>) space.loadClass( name );
        }
        catch ( final Throwable e )
        {
            throw new TypeNotPresentException( name, e );
        }
    }

    public String getName()
    {
        return name;
    }

    public Provider<T> asProvider()
    {
        return this;
    }

    public T get()
    {
        try
        {
            // load class and bootstrap injection
            return injector.getInstance( load() );
        }
        catch ( final Throwable e )
        {
            final String message = "Broken implementation: " + getName();
            LOGGER.error( message, e );
            throw new ProvisionException( message, e );
        }
    }

    public DeferredClass<T> getImplementationClass()
    {
        return this;
    }

    @Override
    public int hashCode()
    {
        return ( 17 * 31 + name.hashCode() ) * 31 + space.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof StrongDeferredClass<?> )
        {
            final StrongDeferredClass<?> clazz = (StrongDeferredClass<?>) rhs;
            return name.equals( clazz.name ) && space.equals( clazz.space );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "Deferred " + name + " from " + space;
    }
}
