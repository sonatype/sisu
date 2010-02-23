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
package org.sonatype.guice.plexus.binders;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.DeferredClass;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

/**
 * {@link Provider} that defers loading the implementation class until it is actually required.
 */
final class DeferredProvider<T>
    implements Provider<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger( DeferredProvider.class );

    @Inject
    private Injector injector;

    private final DeferredClass<T> clazz;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DeferredProvider( final DeferredClass<T> clazz )
    {
        this.clazz = clazz;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public T get()
    {
        try
        {
            // load class and bootstrap bean injection
            return injector.getInstance( clazz.get() );
        }
        catch ( final RuntimeException e )
        {
            final String message = "Cannot create instance of: " + clazz.getName();
            logger.error( message + ": " + e.getMessage() );
            throw new ProvisionException( message, e );
        }
    }
}