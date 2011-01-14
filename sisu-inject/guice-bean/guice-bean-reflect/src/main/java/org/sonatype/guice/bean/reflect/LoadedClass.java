/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.reflect;

/**
 * Pseudo {@link DeferredClass} backed by an already loaded {@link Class}.
 */
public final class LoadedClass<T>
    extends AbstractDeferredClass<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<T> clazz;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public LoadedClass( final Class<? extends T> clazz )
    {
        this.clazz = (Class<T>) clazz;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<T> load()
    {
        return clazz;
    }

    public String getName()
    {
        return clazz.getName();
    }

    @Override
    public int hashCode()
    {
        return clazz.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof LoadedClass<?> )
        {
            return clazz.equals( ( (LoadedClass<?>) rhs ).clazz );
        }
        return false;
    }

    @Override
    public String toString()
    {
        final String id = "Loaded " + clazz;
        final ClassLoader space = clazz.getClassLoader();
        return null != space ? id + " from " + space : id;
    }
}
