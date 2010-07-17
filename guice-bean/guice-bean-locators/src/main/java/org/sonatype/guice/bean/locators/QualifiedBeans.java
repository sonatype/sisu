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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;

/**
 * {@link Iterable} sequence of qualified beans backed by bindings from one or more {@link Injector}s.
 */
class QualifiedBeans<Q extends Annotation, T>
    implements Iterable<QualifiedBean<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final LocatorStrategy strategy;

    private final Key<T> key;

    private List<QualifiedBean<Q, T>> beans = Collections.emptyList();

    private boolean exposed;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedBeans( final Key<T> key )
    {
        this.strategy = selectLocatorStrategy( key );
        this.key = LocatorStrategy.normalize( key );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final synchronized Iterator<QualifiedBean<Q, T>> iterator()
    {
        exposed = true;
        return beans.iterator();
    }

    /**
     * Adds qualified beans from the given injector to the current sequence.
     * 
     * @param injector The new injector
     * @return Added beans
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public synchronized List<QualifiedBean<Q, T>> add( final Injector injector )
    {
        if ( LocatorStrategy.NAMED_WITH_ATTRIBUTES == strategy )
        {
            final Binding binding = injector.getBindings().get( key );
            if ( isVisible( binding ) )
            {
                final Q qualifier = (Q) strategy.findQualifier( key, binding );
                return Collections.singletonList( insertQualifiedBean( qualifier, binding ) );
            }
            return Collections.EMPTY_LIST;
        }

        final List<QualifiedBean<Q, T>> newBeans = new ArrayList<QualifiedBean<Q, T>>();
        for ( final Binding<T> binding : injector.findBindingsByType( key.getTypeLiteral() ) )
        {
            if ( isVisible( binding ) )
            {
                final Q qualifier = (Q) strategy.findQualifier( key, binding );
                if ( null != qualifier )
                {
                    newBeans.add( insertQualifiedBean( qualifier, binding ) );
                }
            }
        }
        return newBeans;
    }

    /**
     * Removes qualified beans from the given injector from the current sequence.
     * 
     * @param injector The old injector
     * @return Removed beans
     */
    public synchronized List<QualifiedBean<Q, T>> remove( final Injector injector )
    {
        if ( beans.isEmpty() )
        {
            return Collections.emptyList();
        }
        // use binding membership to identify which beans belong to removed injector
        final List<QualifiedBean<Q, T>> oldBeans = new ArrayList<QualifiedBean<Q, T>>();
        final Collection<Binding<?>> bindings = injector.getBindings().values();
        for ( int i = 0; i < beans.size(); i++ )
        {
            if ( bindings.contains( beans.get( i ).getBinding() ) )
            {
                oldBeans.add( extractQualifiedBean( i-- ) );
            }
        }
        if ( beans.isEmpty() )
        {
            beans = Collections.emptyList();
        }
        return oldBeans;
    }

    /**
     * Clears all qualified beans from the current sequence.
     * 
     * @return Cleared beans
     */
    public synchronized List<QualifiedBean<Q, T>> clear()
    {
        final List<QualifiedBean<Q, T>> oldBeans = beans;
        beans = Collections.emptyList();
        exposed = false;
        return oldBeans;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Inserts a qualified bean for the given binding into this bean sequence.
     * 
     * @param qualifier The qualifier
     * @param binding The Guice binding
     * @return Qualified bean
     */
    private final QualifiedBean<Q, T> insertQualifiedBean( final Q qualifier, final Binding<T> binding )
    {
        if ( exposed || beans.isEmpty() )
        {
            // take defensive copy to avoid disturbing iterators
            beans = new ArrayList<QualifiedBean<Q, T>>( beans );
            exposed = false;
        }
        final QualifiedBean<Q, T> bean = new QualifiedBean<Q, T>( qualifier, binding );
        if ( LocatorStrategy.isDefaultQualifier( qualifier ) )
        {
            for ( int i = 0, size = beans.size(); i < size; i++ )
            {
                // send default beans to the front, in order of appearance
                if ( !LocatorStrategy.isDefaultQualifier( beans.get( i ).getKey() ) )
                {
                    beans.add( i, bean );
                    return bean;
                }
            }
        }
        beans.add( bean );
        return bean;
    }

    /**
     * Extracts a qualified bean from the given position in this bean sequence.
     * 
     * @param index The bean position
     * @return Qualified bean
     */
    private final QualifiedBean<Q, T> extractQualifiedBean( final int index )
    {
        if ( exposed )
        {
            // take defensive copy to avoid disturbing iterators
            beans = new ArrayList<QualifiedBean<Q, T>>( beans );
            exposed = false;
        }
        return beans.remove( index );
    }

    private static final LocatorStrategy selectLocatorStrategy( final Key<?> expectedKey )
    {
        final Class<?> qualifierType = expectedKey.getAnnotationType();
        if ( null == qualifierType )
        {
            return LocatorStrategy.UNRESTRICTED;
        }
        if ( expectedKey.hasAttributes() )
        {
            if ( expectedKey.getAnnotation() instanceof Named )
            {
                return LocatorStrategy.NAMED_WITH_ATTRIBUTES;
            }
            return LocatorStrategy.MARKED_WITH_ATTRIBUTES;
        }
        if ( qualifierType == Named.class )
        {
            return LocatorStrategy.NAMED;
        }
        return LocatorStrategy.MARKED;
    }

    private static final boolean isVisible( final Binding<?> binding )
    {
        return null != binding && false == binding.getSource() instanceof HiddenSource;
    }
}
