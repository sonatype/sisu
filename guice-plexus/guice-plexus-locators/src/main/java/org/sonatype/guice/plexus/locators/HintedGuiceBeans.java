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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        final Map<String, Entry<String, T>> beanMap = new HashMap();
        if ( null != injectorBeans )
        {
            for ( int i = 0, size = injectorBeans.size(); i < size; i++ )
            {
                for ( final Entry<String, T> e : injectorBeans.get( i ) )
                {
                    final String key = e.getKey();
                    if ( !beanMap.containsKey( key ) )
                    {
                        beanMap.put( e.getKey(), e );
                    }
                }
            }
        }
        final List selectedBeans = new ArrayList( length );
        for ( final String hint : hints )
        {
            final Entry bean = beanMap.get( hint );
            selectedBeans.add( null != bean ? bean : new MissingBean( role, hint ) );
        }
        return selectedBeans.iterator();
    }

    public boolean add( final Injector injector )
    {
        return add( new InjectorBeans<T>( injector, role ) );
    }
}
