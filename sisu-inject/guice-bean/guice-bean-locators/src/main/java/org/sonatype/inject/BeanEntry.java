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

import javax.inject.Provider;

import com.google.inject.Binding;

/**
 * Qualified bean mapping; the Key is the qualifier annotation, the Value is the bean instance.
 */
public interface BeanEntry<Q extends Annotation, T>
    extends Entry<Q, T>, Provider<T>
{
    /**
     * @return Qualifier annotation
     */
    Q getKey();

    /**
     * @return Bean instance (lazily-created)
     */
    T getValue();

    /**
     * @return Human-readable description
     */
    String getDescription();

    /**
     * @return Implementation type
     */
    Class<T> getImplementationClass();

    /**
     * @return Underlying binding
     */
    Binding<? extends T> getBinding();
}
