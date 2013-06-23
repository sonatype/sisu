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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.Legacy;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;

@Deprecated
@Singleton
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    private final org.eclipse.sisu.inject.MutableBeanLocator delegate;

    private final boolean decoupledDelegate;

    @Inject
    public DefaultBeanLocator( final org.eclipse.sisu.inject.MutableBeanLocator delegate )
    {
        this.delegate = delegate;
        decoupledDelegate = false;
    }

    public DefaultBeanLocator()
    {
        delegate = new org.eclipse.sisu.inject.DefaultBeanLocator();
        decoupledDelegate = true;
    }

    public <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> locate( final Key<T> key )
    {
        return Legacy.adapt( delegate.<Q, T> locate( key ) );
    }

    public <Q extends Annotation, T, W> void watch( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        delegate.watch( key, Legacy.adapt( mediator ), watcher );
    }

    public <Q extends Annotation, T, W> void watch( final Key<T> key,
                                                    final org.eclipse.sisu.Mediator<Q, T, W> mediator, final W watcher )
    {
        delegate.watch( key, mediator, watcher );
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

    public void add( final BindingPublisher publisher, final int rank )
    {
        delegate.add( publisher, rank );
    }

    public void remove( final BindingPublisher publisher )
    {
        delegate.remove( publisher );
    }

    @Inject
    void autoPublish( final Injector injector )
    {
        if ( decoupledDelegate )
        {
            final RankingFunction function = injector.getInstance( RankingFunction.class );
            delegate.add( new InjectorPublisher( injector, function ), function.maxRank() );
        }
    }
}