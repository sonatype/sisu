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
package org.eclipse.sisu.inject;

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
        for ( final PropertyBinding b : bindings )
        {
            b.injectProperty( bean );
        }
    }
}
