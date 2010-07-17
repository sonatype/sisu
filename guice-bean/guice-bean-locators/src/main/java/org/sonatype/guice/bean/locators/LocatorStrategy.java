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
import com.google.inject.name.Names;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;

enum LocatorStrategy
{
    UNRESTRICTED
    {
        @Override
        final Annotation findQualifier( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Annotation qualifier = binding.getKey().getAnnotation();
            return null == qualifier ? DEFAULT_QUALIFIER : DEFAULT_QUALIFIER.equals( qualifier ) ? null : qualifier;
        }
    },
    NAMED
    {
        @Override
        final Annotation findQualifier( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Annotation qualifier = UNRESTRICTED.findQualifier( expectedKey, binding );
            return qualifier instanceof Named ? qualifier : null;
        }
    },
    NAMED_WITH_ATTRIBUTES
    {
        @Override
        final Annotation findQualifier( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Annotation qualifier = binding.getKey().getAnnotation();
            return null == qualifier ? DEFAULT_QUALIFIER : qualifier;
        }
    },
    MARKED
    {
        @Override
        final Annotation findQualifier( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Class<? extends Annotation> expectedQualifierType = expectedKey.getAnnotationType();
            final Class<?> implementation = binding.acceptTargetVisitor( TARGET_VISITOR );
            return implementation.getAnnotation( expectedQualifierType );
        }
    },
    MARKED_WITH_ATTRIBUTES
    {
        @Override
        final Annotation findQualifier( final Key<?> expectedKey, final Binding<?> binding )
        {
            final Annotation qualifier = MARKED.findQualifier( expectedKey, binding );
            return expectedKey.getAnnotation().equals( qualifier ) ? qualifier : null;
        }
    };

    abstract Annotation findQualifier( final Key<?> expectedKey, final Binding<?> binding );

    static final Annotation DEFAULT_QUALIFIER = Names.named( "default" );

    static final boolean isDefaultQualifier( final Annotation qualifier )
    {
        return DEFAULT_QUALIFIER.equals( qualifier );
    }

    static final <T> Key<T> normalize( final Key<T> key )
    {
        return isDefaultQualifier( key.getAnnotation() ) ? Key.get( key.getTypeLiteral() ) : key;
    }

    static final QualifiedTargetVisitor TARGET_VISITOR = new QualifiedTargetVisitor();

    static final class QualifiedTargetVisitor
        extends DefaultBindingTargetVisitor<Object, Class<?>>
    {
        @Override
        public Class<?> visit( final LinkedKeyBinding<?> linkedKeyBinding )
        {
            return linkedKeyBinding.getLinkedKey().getTypeLiteral().getRawType();
        }

        @Override
        public Class<?> visit( final ConstructorBinding<?> constructorBinding )
        {
            return constructorBinding.getConstructor().getDeclaringType().getRawType();
        }

        @Override
        public Class<?> visit( final InstanceBinding<?> instanceBinding )
        {
            return instanceBinding.getInstance().getClass();
        }

        @Override
        protected Class<?> visitOther( final Binding<?> binding )
        {
            return Object.class;
        }
    }
}