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
import java.util.List;

import org.sonatype.guice.bean.reflect.Logs;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * {@link Iterable} sequence of qualified beans that notifies a listener whenever the sequence changes.
 */
final class NotifyingBeans<Q extends Annotation, T>
    extends QualifiedBeans<Q, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Runnable listener;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NotifyingBeans( final Key<T> key, final Runnable listener )
    {
        super( key );

        this.listener = listener;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public synchronized List<QualifiedBean<Q, T>> add( final Injector injector )
    {
        return sendEvent( super.add( injector ) );
    }

    @Override
    public synchronized List<QualifiedBean<Q, T>> remove( final Injector injector )
    {
        return sendEvent( super.remove( injector ) );
    }

    @Override
    public synchronized List<QualifiedBean<Q, T>> clear()
    {
        return sendEvent( super.clear() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Notifies the listener that the qualified bean sequence has changed.
     * 
     * @param beans The affected beans
     * @return The affected beans
     */
    private List<QualifiedBean<Q, T>> sendEvent( final List<QualifiedBean<Q, T>> beans )
    {
        if ( !beans.isEmpty() )
        {
            try
            {
                listener.run();
            }
            catch ( final Throwable e )
            {
                Logs.warn( getClass(), "Problem notifying: " + listener.getClass(), e );
            }
        }
        return beans;
    }
}
