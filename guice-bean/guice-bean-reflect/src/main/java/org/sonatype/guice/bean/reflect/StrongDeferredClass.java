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
package org.sonatype.guice.bean.reflect;

/**
 * Strong {@link DeferredClass} representing a named class from a {@link ClassSpace}.
 */
public final class StrongDeferredClass<T>
    extends AbstractDeferredClass<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final String name;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public StrongDeferredClass( final ClassSpace space, final String name )
    {
        this.space = space;
        this.name = name;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public Class<T> load()
    {
        try
        {
            return (Class<T>) space.loadClass( name );
        }
        catch ( final Throwable e )
        {
            throw new TypeNotPresentException( name, e );
        }
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return ( 17 * 31 + name.hashCode() ) * 31 + space.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof StrongDeferredClass<?> )
        {
            final StrongDeferredClass<?> clazz = (StrongDeferredClass<?>) rhs;
            return name.equals( clazz.name ) && space.equals( clazz.space );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "Deferred " + name + " from " + space;
    }
}
