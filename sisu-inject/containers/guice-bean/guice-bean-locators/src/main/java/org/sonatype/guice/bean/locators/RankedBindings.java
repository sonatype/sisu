/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.locators.spi.BindingSubscriber;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Ordered sequence of {@link Binding}s of a given type; subscribes to {@link BindingPublisher}s on demand.
 */
final class RankedBindings<T>
    extends ReentrantLock
    implements Iterable<Binding<T>>, BindingSubscriber<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final RankedSequence<Binding<T>> bindings = new RankedSequence<Binding<T>>();

    final WeakSequence<BeanCache<?, T>> cachedBeans = new WeakSequence<BeanCache<?, T>>();

    final TypeLiteral<T> type;

    final RankedSequence<BindingPublisher> pendingPublishers;

    volatile int topRank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RankedBindings( final TypeLiteral<T> type, final RankedSequence<BindingPublisher> publishers )
    {
        this.type = type;
        this.pendingPublishers = new RankedSequence<BindingPublisher>( publishers );
        topRank = pendingPublishers.topRank();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public TypeLiteral<T> type()
    {
        return type;
    }

    public void add( final Binding<T> binding, final int rank )
    {
        bindings.insert( binding, rank );
    }

    public void remove( final Binding<T> binding )
    {
        if ( bindings.removeThis( binding ) )
        {
            lock();
            try
            {
                for ( final BeanCache<?, T> beans : cachedBeans )
                {
                    beans.remove( binding );
                }
            }
            finally
            {
                unlock();
            }
        }
    }

    public Iterable<Binding<T>> bindings()
    {
        return bindings.snapshot();
    }

    public Itr iterator()
    {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    <Q extends Annotation> BeanCache<Q, T> newBeanCache()
    {
        lock();
        try
        {
            final BeanCache<Q, T> beans = new BeanCache<Q, T>();
            cachedBeans.add( beans );
            return beans;
        }
        finally
        {
            unlock();
        }
    }

    void add( final BindingPublisher publisher, final int rank )
    {
        /*
         * No need to lock; ranked sequence is thread-safe.
         */
        pendingPublishers.insert( publisher, rank );
        if ( rank > topRank )
        {
            topRank = rank;
        }
    }

    void remove( final BindingPublisher publisher )
    {
        /*
         * Lock to prevent race condition with subscriptions.
         */
        lock();
        try
        {
            if ( !pendingPublishers.remove( publisher ) )
            {
                publisher.unsubscribe( this );
            }
        }
        finally
        {
            unlock();
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Binding} iterator that only subscribes to {@link BindingPublisher}s as required.
     */
    final class Itr
        implements Iterator<Binding<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final RankedSequence<Binding<T>>.Itr itr = bindings.iterator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            int rank = topRank;
            if ( rank > Integer.MIN_VALUE && rank > itr.peekNextRank() )
            {
                lock();
                try
                {
                    rank = topRank;
                    while ( rank > Integer.MIN_VALUE && rank > itr.peekNextRank() )
                    {
                        pendingPublishers.poll().subscribe( RankedBindings.this );
                        rank = topRank = pendingPublishers.topRank();
                    }
                }
                finally
                {
                    unlock();
                }
            }
            return itr.hasNext();
        }

        public Binding<T> next()
        {
            return itr.next();
        }

        public int rank()
        {
            return itr.rank();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
