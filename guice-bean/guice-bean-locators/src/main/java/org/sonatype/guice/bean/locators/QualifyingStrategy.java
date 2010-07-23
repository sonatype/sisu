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

import java.lang.annotation.Annotation;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.name.Named;

enum QualifyingStrategy
{
    UNRESTRICTED
    {
        @Override
        final Annotation qualify( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Key<?> key = binding.getKey();
            if ( null != key.getAnnotationType() )
            {
                return key.getAnnotation();
            }
            return QualifiedBeans.DEFAULT_QUALIFIER;
        }
    },
    NAMED
    {
        @Override
        final Annotation qualify( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Annotation qualifier = UNRESTRICTED.qualify( expectedKey, binding );
            return qualifier instanceof Named ? qualifier : null;
        }
    },
    NAMED_WITH_ATTRIBUTES
    {
        @Override
        final Annotation qualify( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Annotation qualifier = NAMED.qualify( expectedKey, binding );
            return expectedKey.getAnnotation().equals( qualifier ) ? qualifier : null;
        }
    },
    MARKED
    {
        @Override
        final Annotation qualify( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Class<? extends Annotation> markerType = expectedKey.getAnnotationType();
            final Class<?> implementation = binding.acceptTargetVisitor( IMPLEMENTATION_VISITOR );
            return implementation.getAnnotation( markerType );
        }
    },
    MARKED_WITH_ATTRIBUTES
    {
        @Override
        final Annotation qualify( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Annotation qualifier = MARKED.qualify( expectedKey, binding );
            return expectedKey.getAnnotation().equals( qualifier ) ? qualifier : null;
        }
    };

    abstract Annotation qualify( final Key<?> expectedKey, final Binding<?> binding );

    static final ImplementationVisitor IMPLEMENTATION_VISITOR = new ImplementationVisitor();
}
