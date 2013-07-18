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

import javax.inject.Inject;

import com.google.inject.Binding;

@Deprecated
public final class DefaultRankingFunction
    implements RankingFunction
{
    private final org.eclipse.sisu.inject.RankingFunction delegate;

    @Inject
    public DefaultRankingFunction( final org.eclipse.sisu.inject.RankingFunction delegate )
    {
        this.delegate = delegate;
    }

    public DefaultRankingFunction( final int primaryRank )
    {
        delegate = new org.eclipse.sisu.inject.DefaultRankingFunction( primaryRank );
    }

    public DefaultRankingFunction()
    {
        this( 0 );
    }

    public int maxRank()
    {
        return delegate.maxRank();
    }

    public <T> int rank( final Binding<T> binding )
    {
        return delegate.rank( binding );
    }
}
