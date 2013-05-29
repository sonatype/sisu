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

@Deprecated
public final class DefaultRankingFunction
    extends org.eclipse.sisu.inject.DefaultRankingFunction
    implements RankingFunction
{
    public DefaultRankingFunction( final int primaryRank )
    {
        super( primaryRank );
    }

    @Inject
    public DefaultRankingFunction()
    {
        this( 0 );
    }
}
