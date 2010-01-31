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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * 
 */
final class HintedGuiceBeans<T>
    extends AbstractGuiceBeans<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeLiteral<T> role;

    private final String[] hints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HintedGuiceBeans( final TypeLiteral<T> role, final String[] hints )
    {
        this.role = role;
        this.hints = hints;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public synchronized Iterator<Entry<String, T>> iterator()
    {
        final int length = hints.length;

        final List combinedBeans;
        final int numInjectorBeans;

        if ( null != injectorBeans )
        {
            combinedBeans = new ArrayList( injectorBeans.get( 0 ) );
            numInjectorBeans = injectorBeans.size();
        }
        else
        {
            combinedBeans = new ArrayList( Collections.nCopies( length, null ) );
            numInjectorBeans = 0;
        }

        for ( int h = 0; h < length; h++ )
        {
            int index = 1;
            while ( combinedBeans.get( h ) == null )
            {
                if ( index < numInjectorBeans )
                {
                    combinedBeans.set( h, injectorBeans.get( index++ ).get( h ) );
                }
                else
                {
                    combinedBeans.set( h, new MissingBean( role, hints[h] ) );
                }
            }
        }
        return combinedBeans.iterator();
    }

    public boolean add( final Injector injector )
    {
        return add( new HintedInjectorBeans<T>( injector, role, hints ) );
    }
}
