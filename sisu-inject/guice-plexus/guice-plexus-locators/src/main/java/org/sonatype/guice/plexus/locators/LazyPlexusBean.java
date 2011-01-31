package org.sonatype.guice.plexus.locators;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.util.Map.Entry;

import org.sonatype.guice.plexus.config.PlexusBean;
import org.sonatype.inject.BeanEntry;

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

    private final BeanEntry<Named, T> bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyPlexusBean( final BeanEntry<Named, T> bean )
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
        return bean.getDescription();
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
