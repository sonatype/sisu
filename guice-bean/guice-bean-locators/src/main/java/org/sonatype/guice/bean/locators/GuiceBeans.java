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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * {@link Iterable} sequence of qualified beans backed by bindings from one or more {@link Injector}s.
 */
class GuiceBeans<Q extends Annotation, T>
    implements Iterable<Entry<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Key<T> key;

    private List<InjectorBeans<Q, T>> injectorBeans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    GuiceBeans( final Key<T> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final synchronized Iterator<Entry<Q, T>> iterator()
    {
        if ( null == injectorBeans )
        {
            return Collections.<Entry<Q, T>> emptyList().iterator();
        }
        int defaultIndex = 0;
        final List<Entry<Q, T>> combinedBeans = new ArrayList<Entry<Q, T>>();
        for ( final InjectorBeans<Q, T> beans : injectorBeans )
        {
            for ( final Entry<Q, T> entry : beans )
            {
                if ( BeanLocator.DEFAULT == entry.getKey() )
                {
                    combinedBeans.add( defaultIndex++, entry );
                }
                else
                {
                    combinedBeans.add( entry );
                }
            }
        }
        return combinedBeans.iterator();
    }

    /**
     * Adds qualified beans from the given injector to the current sequence.
     * 
     * @param injector The new injector
     * @return Added beans
     */
    public synchronized List<Entry<Q, T>> add( final Injector injector )
    {
        final InjectorBeans<Q, T> newBeans = new InjectorBeans<Q, T>( injector, key );
        if ( !newBeans.isEmpty() )
        {
            if ( null == injectorBeans )
            {
                injectorBeans = new ArrayList<InjectorBeans<Q, T>>( 4 );
            }
            injectorBeans.add( newBeans );
        }
        return newBeans;
    }

    /**
     * Removes qualified beans from the given injector from the current sequence.
     * 
     * @param injector The old injector
     * @return Removed beans
     */
    public synchronized List<Entry<Q, T>> remove( final Injector injector )
    {
        if ( null != injectorBeans )
        {
            for ( final InjectorBeans<Q, T> beans : injectorBeans )
            {
                if ( injector == beans.injector )
                {
                    injectorBeans.remove( beans );
                    if ( injectorBeans.isEmpty() )
                    {
                        injectorBeans = null;
                    }
                    return beans;
                }
            }
        }
        return Collections.emptyList();
    }
}
