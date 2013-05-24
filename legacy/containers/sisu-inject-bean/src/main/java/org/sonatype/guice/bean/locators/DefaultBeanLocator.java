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
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Key;

@Deprecated
@Singleton
public final class DefaultBeanLocator
    implements BeanLocator
{
    @Inject
    private org.eclipse.sisu.inject.BeanLocator locator;

    public <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> locate( final Key<T> key )
    {
        final Iterable<org.eclipse.sisu.BeanEntry<Q, T>> i = locator.locate( key );
        return new Iterable<BeanEntry<Q, T>>()
        {
            public Iterator<BeanEntry<Q, T>> iterator()
            {
                final Iterator<org.eclipse.sisu.BeanEntry<Q, T>> itr = i.iterator();
                return new Iterator<BeanEntry<Q, T>>()
                {
                    public boolean hasNext()
                    {
                        return itr.hasNext();
                    }

                    public BeanEntry<Q, T> next()
                    {
                        return adapt( itr.next() );
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
        locator.watch( key, new org.eclipse.sisu.Mediator<Q, T, W>()
        {
            public void add( org.eclipse.sisu.BeanEntry<Q, T> entry, W w )
                throws Exception
            {
                mediator.add( adapt( entry ), w );
            }

            public void remove( org.eclipse.sisu.BeanEntry<Q, T> entry, W w )
                throws Exception
            {
                mediator.remove( adapt( entry ), w );
            }
        }, watcher );
    }

    static <Q extends Annotation, T> BeanEntry<Q, T> adapt( final org.eclipse.sisu.BeanEntry<Q, T> delegate )
    {
        return new BeanEntry<Q, T>()
        {
            public Q getKey()
            {
                return delegate.getKey();
            }

            public T getValue()
            {
                return delegate.getValue();
            }

            public T setValue( T value )
            {
                return delegate.setValue( value );
            }

            public Provider<T> getProvider()
            {
                return delegate.getProvider();
            }

            public String getDescription()
            {
                return delegate.getDescription();
            }

            public Class<T> getImplementationClass()
            {
                return delegate.getImplementationClass();
            }

            public Object getSource()
            {
                return delegate.getSource();
            }

            public int getRank()
            {
                return delegate.getRank();
            }
        };
    }
}
