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
    @Inject
    private org.eclipse.sisu.inject.MutableBeanLocator delegate;

    public <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> locate( final Key<T> key )
    {
        return Legacy.adapt( delegate.<Q, T> locate( key ) );
    }

    public <Q extends Annotation, T, W> void watch( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        delegate.watch( key, Legacy.adapt( mediator ), watcher );
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
