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
package org.sonatype.guice.bean.inject;

import java.util.Collection;

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

    BeanInjector( final Collection<PropertyBinding> bindings )
    {
        int n = bindings.size();
        this.bindings = new PropertyBinding[n];
        for ( final PropertyBinding b : bindings )
        {
            this.bindings[--n] = b; // inject properties in reverse order of discovery
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

    /**
     * @return {@code true} if this thread is performing bean injection; otherwise {@code false}
     */
    public static boolean isInjecting()
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
