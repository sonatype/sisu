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

import org.sonatype.guice.bean.locators.QualifiedBean;
import org.sonatype.guice.plexus.config.PlexusBean;
import org.sonatype.guice.plexus.config.PlexusBeanDescription;

import com.google.inject.name.Named;

/**
 * {@link Entry} representing a lazy @{@link Named} Plexus bean; the bean is only retrieved when the value is requested.
 */
final class LazyPlexusBean<T>
    implements PlexusBean<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final QualifiedBean<Named, T> bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyPlexusBean( final QualifiedBean<Named, T> bean )
    {
        this.bean = bean;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getKey()
    {
        return bean.getKey().value();
    }

    public T getValue()
    {
        return bean.getValue();
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public String getDescription()
    {
        final Object source = bean.getBinding().getSource();
        if ( source instanceof PlexusBeanDescription )
        {
            return ( (PlexusBeanDescription) source ).getDescription();
        }
        return null;
    }

    public Class<T> getImplementationClass()
    {
        return bean.getImplementationClass();
    }

    @Override
    public String toString()
    {
        return getKey() + "=" + getValue();
    }
}
