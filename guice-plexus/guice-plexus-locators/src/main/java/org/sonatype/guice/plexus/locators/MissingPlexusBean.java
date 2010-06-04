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

import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

/**
 * {@link Entry} representing a missing @{@link Named} Plexus bean.
 */
final class MissingPlexusBean<T>
    implements PlexusBeanLocator.Bean<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeLiteral<T> role;

    private final String hint;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MissingPlexusBean( final TypeLiteral<T> role, final String hint )
    {
        this.role = role;
        this.hint = hint;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getKey()
    {
        return hint;
    }

    public T getValue()
    {
        return Roles.throwMissingComponentException( role, hint );
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public String getDescription()
    {
        return null;
    }

    public DeferredClass<T> getImplementationClass()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return getKey() + "=<missing>";
    }
}
