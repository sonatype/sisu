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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Weak {@link DeferredClass} representing a named class from a {@link ClassSpace}.
 */
public final class WeakDeferredClass<T>
    implements DeferredClass<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final String name;

    private Reference<Class<T>> clazzRef;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public WeakDeferredClass( final ClassSpace space, final String name )
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
        Class clazz = null != clazzRef ? clazzRef.get() : null;
        if ( null == clazz )
        {
            try
            {
                clazz = space.loadClass( name );
                clazzRef = new WeakReference( clazz );
            }
            catch ( final Throwable e )
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
