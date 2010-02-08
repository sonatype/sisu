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
package org.sonatype.guice.plexus.locators;

import java.util.Map.Entry;

import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.guice.plexus.config.PlexusBeanLocator;

/**
 * {@link Entry} representing a lazy @{@link Named} Plexus bean; the bean is only retrieved when the value is requested.
 */
final class LazyBean<T>
    implements PlexusBeanLocator.Bean<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String hint;

    private Provider<T> provider;

    private T bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyBean( final String hint, final Provider<T> provider )
    {
        this.hint = hint;
        this.provider = provider;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getKey()
    {
        return hint;
    }

    public synchronized T getValue()
    {
        if ( null != provider )
        {
            // lazy bean creation
            bean = provider.get();
            provider = null;
        }
        return bean; // from now on return same bean
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }
}