/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.Down;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;

@Deprecated
@Singleton
@SuppressWarnings( "unchecked" )
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    @Inject
    private org.eclipse.sisu.inject.MutableBeanLocator delegate;

    public <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> locate( final Key<T> key )
    {
        final Iterable<org.eclipse.sisu.BeanEntry<Q, T>> beans = delegate.locate( key );
        return new Iterable<BeanEntry<Q, T>>()
        {
            public Iterator<BeanEntry<Q, T>> iterator()
            {
                final Iterator<org.eclipse.sisu.BeanEntry<Q, T>> itr = beans.iterator();
                return new Iterator<BeanEntry<Q, T>>()
                {
                    public boolean hasNext()
                    {
                        return itr.hasNext();
                    }

                    public BeanEntry<Q, T> next()
                    {
                        return Down.cast( BeanEntry.class, itr.next() );
                    }

                    public void remove()
                    {
                        itr.remove();
                    }
                };
            }
        };
    }

    public <Q extends Annotation, T, W> void watch( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        delegate.watch( key, new org.eclipse.sisu.Mediator<Q, T, W>()
        {
            public void add( final org.eclipse.sisu.BeanEntry<Q, T> entry, final W w )
                throws Exception
            {
                mediator.add( Down.cast( BeanEntry.class, entry ), w );
            }

            public void remove( final org.eclipse.sisu.BeanEntry<Q, T> entry, final W w )
                throws Exception
            {
                mediator.remove( Down.cast( BeanEntry.class, entry ), w );
            }
        }, watcher );
    }

    public void add( final Injector injector, final int rank )
    {
        delegate.add( injector, rank );
    }

    public void remove( final Injector injector )
    {
        delegate.remove( injector );
    }

    public void clear()
    {
        delegate.clear();
    }
}
