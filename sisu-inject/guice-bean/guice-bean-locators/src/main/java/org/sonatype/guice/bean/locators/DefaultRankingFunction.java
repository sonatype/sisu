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

import com.google.inject.Binding;

public final class DefaultRankingFunction
    implements RankingFunction
{
    private final int rank;

    public DefaultRankingFunction()
    {
        this( 0 );
    }

    public DefaultRankingFunction( final int rank )
    {
        if ( rank < 0 )
        {
            throw new IllegalArgumentException( "Default rank must be zero or more" );
        }
        this.rank = rank;
    }

    public int maxRank()
    {
        return rank;
    }

    public <T> int rank( final Binding<T> binding )
    {
        return null == binding.getKey().getAnnotationType() ? rank : rank - Integer.MAX_VALUE;
    }
}