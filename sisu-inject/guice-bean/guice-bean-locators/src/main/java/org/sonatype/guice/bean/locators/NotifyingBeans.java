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

final class NotifyingBeans<Q extends Annotation, T>
    extends QualifiedBeans<Q, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Runnable notify;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NotifyingBeans( final Key<T> key, final Runnable notify )
    {
        super( key );

        this.notify = notify;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public synchronized List<QualifiedBean<Q, T>> add( final Injector injector )
    {
        return sendUpdate( super.add( injector ) );
    }

    @Override
    public synchronized List<QualifiedBean<Q, T>> remove( final Injector injector )
    {
        return sendUpdate( super.remove( injector ) );
    }

    @Override
    public synchronized List<QualifiedBean<Q, T>> clear()
    {
        return sendUpdate( super.clear() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private final List<QualifiedBean<Q, T>> sendUpdate( final List<QualifiedBean<Q, T>> beans )
    {
        if ( !beans.isEmpty() )
        {
            try
            {
                notify.run();
            }
            catch ( final Throwable e )
            {
                Logs.warn( getClass(), "Problem notifying: " + notify.getClass(), e );
            }
        }
        return beans;
    }
}
