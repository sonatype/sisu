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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Named;

import com.google.inject.Binding;
import com.google.inject.Key;

final class QualifiedBeans<Q extends Annotation, T>
    implements Iterable<QualifiedBean<Q, T>>, BeanCache<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private Map<Binding<T>, QualifiedBean<Q, T>> beanCache;

    final Key<T> key;

    final Iterable<Binding<T>> bindings;

    final QualifyingStrategy strategy;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedBeans( final Key<T> key, final Iterable<Binding<T>> bindings )
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

    public synchronized void keepAlive( final List<Binding<T>> activeBindings )
    {
        if ( null != beanCache )
        {
            beanCache.keySet().retainAll( activeBindings );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    static QualifyingStrategy selectQualifyingStrategy( final Key<?> key )
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

    synchronized QualifiedBean<Q, T> saveBean( final Q qualifier, final Binding<T> binding )
    {
        if ( null == beanCache )
        {
            beanCache = new IdentityHashMap<Binding<T>, QualifiedBean<Q, T>>();
        }
        QualifiedBean<Q, T> bean = beanCache.get( binding );
        if ( null == bean )
        {
            bean = new LazyQualifiedBean<Q, T>( qualifier, binding );
            beanCache.put( binding, bean );
        }
        return bean;
    }

    synchronized QualifiedBean<Q, T> loadBean( final Binding<T> binding )
    {
        return null != beanCache ? beanCache.get( binding ) : null;
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
                nextBean = loadBean( binding );
                if ( null != nextBean )
                {
                    return true;
                }
                @SuppressWarnings( "unchecked" )
                final Q qualifier = (Q) strategy.qualifies( key, binding );
                if ( null != qualifier )
                {
                    nextBean = saveBean( qualifier, binding );
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
