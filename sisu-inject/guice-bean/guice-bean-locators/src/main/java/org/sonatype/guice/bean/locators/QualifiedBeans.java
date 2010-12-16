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

import static org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS;
import static org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.ReferenceType.STRONG;
import static org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.ReferenceType.WEAK;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Named;

import org.sonatype.guice.bean.locators.ConcurrentReferenceHashMap.Option;

import com.google.inject.Binding;
import com.google.inject.Key;

final class QualifiedBeans<Q extends Annotation, T>
    implements Iterable<QualifiedBean<Q, T>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final EnumSet<Option> IDENTITY = EnumSet.of( IDENTITY_COMPARISONS );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Key<T> key;

    final RankedBindings<T> bindings;

    final QualifyingStrategy strategy;

    final ConcurrentMap<Binding<T>, QualifiedBean<Q, T>> beanMap =
        new ConcurrentReferenceHashMap<Binding<T>, QualifiedBean<Q, T>>( 16, 0.75f, 1, WEAK, STRONG, IDENTITY );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedBeans( final Key<T> key, final RankedBindings<T> bindings )
    {
        this.key = key;
        this.bindings = bindings;

        strategy = selectQualifyingStrategy( key );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<QualifiedBean<Q, T>> iterator()
    {
        return new QualifiedBeanIterator();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    static final QualifyingStrategy selectQualifyingStrategy( final Key<?> key )
    {
        final Class<?> qualifierType = key.getAnnotationType();
        if ( null == qualifierType )
        {
            return QualifyingStrategy.UNRESTRICTED;
        }
        if ( Named.class == qualifierType )
        {
            return key.hasAttributes() ? QualifyingStrategy.NAMED_WITH_ATTRIBUTES : QualifyingStrategy.NAMED;
        }
        return key.hasAttributes() ? QualifyingStrategy.MARKED_WITH_ATTRIBUTES : QualifyingStrategy.MARKED;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    final class QualifiedBeanIterator
        implements Iterator<QualifiedBean<Q, T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Binding<T>> itr = bindings.iterator();

        private QualifiedBean<Q, T> nextBean;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            if ( null != nextBean )
            {
                return true;
            }
            while ( itr.hasNext() )
            {
                final Binding<T> binding = itr.next();
                nextBean = beanMap.get( binding );
                if ( null != nextBean )
                {
                    return true;
                }
                @SuppressWarnings( "unchecked" )
                final Q qualifier = (Q) strategy.qualifies( key, binding );
                if ( null != qualifier )
                {
                    nextBean = new LazyQualifiedBean<Q, T>( qualifier, binding );
                    final QualifiedBean<Q, T> oldBean = beanMap.putIfAbsent( binding, nextBean );
                    if ( null != oldBean )
                    {
                        nextBean = oldBean;
                    }
                    return true;
                }
            }
            return false;
        }

        public QualifiedBean<Q, T> next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
                final QualifiedBean<Q, T> bean = nextBean;
                nextBean = null;
                return bean;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
