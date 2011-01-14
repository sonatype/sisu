/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
import com.google.inject.ImplementedBy;

/**
 * Assigns each {@link Binding} a rank according to some function; higher ranks take precedence over lower ranks.
 */
@ImplementedBy( DefaultRankingFunction.class )
public interface RankingFunction
{
    /**
     * Estimates the maximum possible rank for this function; used to arrange injectors in order of ranking probability.
     * 
     * @return Maximum rank
     */
    int maxRank();

    /**
     * Assigns a numeric rank to the given binding.
     * 
     * @param binding The binding
     * @return Assigned rank
     */
    <T> int rank( Binding<T> binding );
}
