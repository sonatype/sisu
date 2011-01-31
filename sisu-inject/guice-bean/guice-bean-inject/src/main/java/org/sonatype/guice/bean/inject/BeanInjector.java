/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

package org.sonatype.guice.bean.inject;

import java.util.List;

import com.google.inject.MembersInjector;

/**
 * {@link MembersInjector} that takes {@link PropertyBinding}s and applies them to bean instances.
 */
final class BeanInjector<B>
    implements MembersInjector<B>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ThreadLocal<boolean[]> isInjectingHolder = new ThreadLocal<boolean[]>()
    {
        @Override
        protected boolean[] initialValue()
        {
            return new boolean[] { false };
        }
    };

    private final PropertyBinding[] bindings;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanInjector( final List<PropertyBinding> bindings )
    {
        final int size = bindings.size();
        this.bindings = new PropertyBinding[size];
        for ( int i = 0, n = size; i < size; )
        {
            // reverse: inject superclass before sub
            this.bindings[i++] = bindings.get( --n );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void injectMembers( final B bean )
    {
        final boolean[] isInjecting = isInjectingHolder.get();
        if ( !isInjecting[0] )
        {
            isInjecting[0] = true;
            try
            {
                doInjection( bean );
            }
            finally
            {
                isInjecting[0] = false;
            }
        }
        else
        {
            // nested injection
            doInjection( bean );
        }
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * @return {@code true} if this thread is performing bean injection; otherwise {@code false}
     */
    static boolean isInjecting()
    {
        return isInjectingHolder.get()[0];
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Injects properties into the given bean.
     * 
     * @param bean The bean to inject
     */
    private void doInjection( final Object bean )
    {
        for ( final PropertyBinding b : bindings )
        {
            b.injectProperty( bean );
        }
    }
}
