/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.reflect;

import javax.inject.Inject;

import com.google.inject.Injector;

/**
 * Abstract combination of {@link DeferredClass} and {@link DeferredProvider}.
 */
abstract class AbstractDeferredClass<T>
    implements DeferredClass<T>, DeferredProvider<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private Injector injector;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final DeferredProvider<T> asProvider()
    {
        return this;
    }

    public final T get()
    {
        try
        {
            // load class and bootstrap injection
            return injector.getInstance( load() );
        }
        catch ( final Throwable e )
        {
            Logs.catchThrowable( e );
            try
            {
                Logs.warn( "Error injecting: {}", getName(), e );
            }
            finally
            {
                Logs.throwUnchecked( e );
            }
        }
        return null; // not used
    }

    public final DeferredClass<T> getImplementationClass()
    {
        return this;
    }
}
