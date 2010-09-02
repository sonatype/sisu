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
package org.sonatype.guice.bean.locators;

import javax.inject.Provider;

import org.sonatype.inject.Mediator;

import com.google.inject.name.Named;

/**
 * Delegating {@link Mediator} that automatically unwraps {@link Named} values.
 */
public final class NamedMediatorAdapter<T, W>
    implements Mediator<Named, T, W>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Mediator<String, T, W> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public NamedMediatorAdapter( final Mediator<String, T, W> delegate )
    {
        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void add( final Named qualifier, final Provider<T> bean, final W watcher )
        throws Exception
    {
        delegate.add( qualifier.value(), bean, watcher );
    }

    public void remove( final Named qualifier, final Provider<T> bean, final W watcher )
        throws Exception
    {
        delegate.remove( qualifier.value(), bean, watcher );
    }
}
