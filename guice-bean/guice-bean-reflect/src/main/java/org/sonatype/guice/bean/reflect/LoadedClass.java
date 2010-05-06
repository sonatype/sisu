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
package org.sonatype.guice.bean.reflect;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

/**
 * Pseudo {@link DeferredClass} backed by an already loaded {@link Class}.
 */
public final class LoadedClass<T>
    implements DeferredClass<T>, DeferredProvider<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger( LoadedClass.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<T> clazz;

    @Inject
    private Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public LoadedClass( final Class<? extends T> clazz )
    {
        this.clazz = (Class<T>) clazz;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<T> load()
    {
        return clazz;
    }

    public String getName()
    {
        return clazz.getName();
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
        return clazz.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof LoadedClass<?> )
        {
            return clazz.equals( ( (LoadedClass<?>) rhs ).clazz );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "Loaded " + clazz;
    }
}
