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
