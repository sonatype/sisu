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
package org.sonatype.inject;

import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import javax.inject.Qualifier;

/**
 * Map {@link Entry} that maps a JSR330 @{@link Qualifier} annotation to a bean implementation.
 */
public interface BeanEntry<Q extends Annotation, T>
    extends Entry<Q, T>
{
    /**
     * Returns the @{@link Qualifier} annotation associated with this particular bean.
     * 
     * @return Qualifier annotation
     */
    Q getKey();

    /**
     * Creates a bean instance; returns the same instance for each subsequent call.
     * 
     * @return Bean instance (lazily-created)
     */
    T getValue();

    /**
     * Returns a human-readable description of the bean; see @{@link Description}.
     * 
     * @return Human-readable description
     */
    String getDescription();

    /**
     * Attempts to find the implementation type without creating the bean instance.
     * 
     * @return Implementation type; {@code null} if the type cannot be determined
     */
    Class<T> getImplementationClass();

    /**
     * Returns an arbitrary object that describes where this bean was configured.
     * 
     * @return Source of the bean
     */
    Object getSource();

    /**
     * Returns the bean's rank; higher ranked beans override lower ranked beans.
     * 
     * @return Assigned rank
     */
    int getRank();
}
