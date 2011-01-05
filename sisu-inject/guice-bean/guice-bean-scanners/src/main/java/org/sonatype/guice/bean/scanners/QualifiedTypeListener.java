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
package org.sonatype.guice.bean.scanners;

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import com.google.inject.Binder;

/**
 * Listens for types annotated with {@link Qualifier} annotations.
 */
public interface QualifiedTypeListener
{
    /**
     * Invoked when the {@link QualifiedTypeVisitor} finds a qualified type.
     * 
     * @param qualifier The qualifier
     * @param qualifiedType The qualified type
     * @param source The source of this type
     * @see Binder#withSource(Object)
     */
    void hear( Annotation qualifier, Class<?> qualifiedType, Object source );
}
