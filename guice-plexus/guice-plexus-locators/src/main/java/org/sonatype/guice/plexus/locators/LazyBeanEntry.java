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

import javax.inject.Provider;

/**
 * {@link Entry} that represents a lazy Plexus bean; the bean is only retrieved when the value is requested.
 */
final class LazyBeanEntry<T>
    implements Entry<String, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String hint;

    private Provider<T> provider;

    private T value;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyBeanEntry( final String hint, final Provider<T> provider )
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
            value = provider.get();
            provider = null;
        }
        return value; // from now on return same bean
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }
}