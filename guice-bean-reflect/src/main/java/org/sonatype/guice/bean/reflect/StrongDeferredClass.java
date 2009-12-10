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
package org.sonatype.guice.bean.reflect;

/**
 * Strong {@link DeferredClass} representing a named class from a {@link ClassSpace}.
 */
public final class StrongDeferredClass<T>
    implements DeferredClass<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final String name;

    private Class<T> clazz;

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
    public Class<T> get()
    {
        if ( null == clazz )
        {
            try
            {
                clazz = (Class) space.loadClass( name );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( name, e );
            }
        }
        return clazz;
    }

    public String getName()
    {
        return name;
    }
}