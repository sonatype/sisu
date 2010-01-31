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

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Injector;

/**
 * 
 */
abstract class AbstractGuiceBeans<T>
    implements GuiceBeans<T>
{
    // ----------------------------------------------------------------------
    // Common fields
    // ----------------------------------------------------------------------

    List<InjectorBeans<T>> injectorBeans;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final boolean add( final InjectorBeans<T> newBeans )
    {
        if ( newBeans.isEmpty() )
        {
            return false;
        }
        synchronized ( this )
        {
            if ( null == injectorBeans )
            {
                injectorBeans = new ArrayList<InjectorBeans<T>>( 4 );
            }
            for ( final InjectorBeans<T> beans : injectorBeans )
            {
                if ( newBeans.injector == beans.injector )
                {
                    return false;
                }
            }
            return injectorBeans.add( newBeans );
        }
    }

    public final boolean remove( final Injector injector )
    {
        synchronized ( this )
        {
            if ( null == injectorBeans )
            {
                return false;
            }
            for ( final InjectorBeans<T> beans : injectorBeans )
            {
                if ( injector == beans.injector )
                {
                    return injectorBeans.remove( beans );
                }
            }
        }
        return false;
    }
}
