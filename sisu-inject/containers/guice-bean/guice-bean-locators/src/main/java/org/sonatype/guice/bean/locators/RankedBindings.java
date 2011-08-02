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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.locators.spi.BindingSubscriber;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Ordered sequence of {@link Binding}s of a given type; subscribes to {@link BindingPublisher}s on demand.
 */
final class RankedBindings<T>
    implements Iterable<Binding<T>>, BindingDistributor, BindingSubscriber<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final RankedSequence<Binding<T>> bindings = new RankedSequence<Binding<T>>();

    final List<Reference<LocatedBeans<?, T>>> locatedBeanRefs = new ArrayList<Reference<LocatedBeans<?, T>>>();

    final RankedSequence<BindingPublisher> pendingPublishers;

    final TypeLiteral<T> type;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RankedBindings( final TypeLiteral<T> type, final RankedSequence<BindingPublisher> publishers )
    {
        pendingPublishers = null != publishers ? publishers.clone() : new RankedSequence<BindingPublisher>();
        this.type = type;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void add( final BindingPublisher publisher, final int rank )
    {
        synchronized ( pendingPublishers )
        {
            pendingPublishers.insert( publisher, rank );
        }
    }

    public void remove( final BindingPublisher publisher )
    {
        synchronized ( pendingPublishers )
        {
            // extra cleanup if we're already subscribed
            if ( !pendingPublishers.remove( publisher ) )
            {
                publisher.unsubscribe( this );
                synchronized ( bindings )
                {
                    boolean updated = false;
                    for ( int i = 0; i < bindings.size(); i++ )
                    {
                        if ( publisher.containsThis( bindings.get( i ) ) )
                        {
                            bindings.remove( i-- );
                            updated = true;
                        }
                    }
                    if ( updated )
                    {
                        evictStaleBeanEntries();
                    }
                }
            }
        }
    }

    public TypeLiteral<T> type()
    {
        return type;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void add( final Binding binding, final int rank )
    {
        synchronized ( bindings )
        {
            bindings.insert( binding, rank );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void remove( final Binding binding )
    {
        synchronized ( bindings )
        {
            // we only want to remove this _exact_ instance
            final int index = bindings.indexOfThis( binding );
            if ( index >= 0 )
            {
                bindings.remove( index );
            }
        }
    }

    public void clear()
    {
        synchronized ( pendingPublishers )
        {
            pendingPublishers.clear();
            synchronized ( bindings )
            {
                bindings.clear();
                evictStaleBeanEntries();
            }
        }
    }

    public Itr iterator()
    {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Associates the given {@link LocatedBeans} with this binding sequence so stale beans can be eagerly evicted.
     * 
     * @param beans The located beans
     */
    <Q extends Annotation> void linkToBeans( final LocatedBeans<Q, T> beans )
    {
        synchronized ( locatedBeanRefs )
        {
            final Reference<LocatedBeans<?, T>> beanRef = new WeakReference<LocatedBeans<?, T>>( beans );
            for ( int i = 0, size = locatedBeanRefs.size(); i < size; i++ )
            {
                if ( null == locatedBeanRefs.get( i ).get() )
                {
                    // replace evicted element
                    locatedBeanRefs.set( i, beanRef );
                    return; // bail-out early
                }
            }
            locatedBeanRefs.add( beanRef );
        }
    }

    /**
     * @return {@code true} if this binding sequence is still associated with active beans; otherwise {@code false}
     */
    boolean isActive()
    {
        boolean isActive = false;
        synchronized ( locatedBeanRefs )
        {
            for ( int i = 0; i < locatedBeanRefs.size(); i++ )
            {
                if ( null != locatedBeanRefs.get( i ).get() )
                {
                    isActive = true;
                }
                else
                {
                    locatedBeanRefs.remove( i-- );
                }
            }
        }
        return isActive;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Evicts any stale bean entries from the associated {@link LocatedBeans}.
     */
    private void evictStaleBeanEntries()
    {
        synchronized ( locatedBeanRefs )
        {
            for ( int i = 0, size = locatedBeanRefs.size(); i < size; i++ )
            {
                final LocatedBeans<?, T> beans = locatedBeanRefs.get( i ).get();
                if ( null != beans )
                {
                    beans.retainAll( bindings );
                }
            }
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
            if ( pendingPublishers.size() > 0 )
            {
                synchronized ( pendingPublishers )
                {
                    // check whether the next publisher _might_ contain a higher ranked binding, and if so use it
                    while ( pendingPublishers.size() > 0 && pendingPublishers.getRank( 0 ) > itr.peekNextRank() )
                    {
                        // be careful not to remove the pending publisher until after it's used
                        // otherwise another iterator could skip past the initial size() check!
                        pendingPublishers.get( 0 ).subscribe( RankedBindings.this );
                        pendingPublishers.remove( 0 );
                    }
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
