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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.guice.bean.reflect.DeferredClass;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;

/**
 * Keeps track of deferred injection requests so we can process them later without worrying about cyclic dependencies.
 */
@Singleton
final class DeferredInjector
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private Injector injector;

    private final List<Object> deferredInjectees = new ArrayList<Object>();

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Records an injection request so it can be deferred until after the primary objects have been created.
     * 
     * @param injectee The bean to be injected
     */
    synchronized <T> T deferInjection( final T injectee )
    {
        deferredInjectees.add( injectee );
        return injectee;
    }

    /**
     * Iterates over list of deferred injectees, injecting their members (this may cause the list to grow).
     */
    synchronized Injector resumeInjections()
    {
        while ( !deferredInjectees.isEmpty() )
        {
            // this might eventually add more entries to the list
            injector.injectMembers( deferredInjectees.remove( 0 ) );
        }
        return injector;
    }

    // ----------------------------------------------------------------------
    // Shared helpers
    // ----------------------------------------------------------------------

    /**
     * {@link Provider} that creates instances of a {@link DeferredClass} but defers injection until later on.
     */
    static final class DeferredProvider<T>
        implements Provider<T>
    {
        @Inject
        private DeferredInjector injector;

        private final DeferredClass<T> clazz;

        /**
         * Creates a {@link Provider} that defers member injection until a later date.
         * 
         * @param clazz The deferred class
         */
        DeferredProvider( final DeferredClass<T> clazz )
        {
            this.clazz = clazz;
        }

        /**
         * @return Instance of the deferred class (before member injection has occurred)
         */
        public T get()
        {
            try
            {
                return injector.deferInjection( clazz.get().newInstance() );
            }
            catch ( final Exception e )
            {
                throw new ProvisionException( "Cannot create instance of: " + clazz.getName(), e );
            }
        }
    }
}
