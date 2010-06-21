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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * {@link Iterable} sequence of qualified beans backed by bindings from one or more {@link Injector}s.
 */
class QualifiedBeans<Q extends Annotation, T>
    implements Iterable<Entry<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Key<T> key;

    private List<DeferredBeanEntry<Q, T>> beans = Collections.emptyList();

    private int defaultIndex;

    private boolean exposed;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedBeans( final Key<T> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public final synchronized Iterator<Entry<Q, T>> iterator()
    {
        exposed = true; // flag that someone is using the beans

        return (Iterator) beans.iterator();
    }

    /**
     * Adds qualified beans from the given injector to the current sequence.
     * 
     * @param injector The new injector
     * @return Added beans
     */
    @SuppressWarnings( "unchecked" )
    public synchronized List<Entry<Q, T>> add( final Injector injector )
    {
        if ( key.hasAttributes() )
        {
            // optimization for looking up a specific qualified bean
            final Binding binding = injector.getBindings().get( key );
            if ( null != binding && isVisible( binding ) )
            {
                return Collections.singletonList( insertQualifiedBean( binding ) );
            }
            return Collections.EMPTY_LIST;
        }

        final TypeLiteral<T> beanType = key.getTypeLiteral();
        final Class<?> qualifierType = key.getAnnotationType();
        final boolean anyQualifier = qualifierType == null;

        // looking for any explicit binding that implements this type
        final List<Entry<Q, T>> newBeans = new ArrayList<Entry<Q, T>>();
        for ( final Binding<T> binding : injector.findBindingsByType( beanType ) )
        {
            if ( isVisible( binding ) )
            {
                // ignore unqualified bindings to avoid duplicating @Named defaults
                // @see QualifiedTypeBinder -> bindQualifiedType(Annotation, Class)
                final Class<?> annType = binding.getKey().getAnnotationType();
                if ( null != annType && ( anyQualifier || qualifierType == annType ) )
                {
                    newBeans.add( insertQualifiedBean( binding ) );
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
    public synchronized List<Entry<Q, T>> remove( final Injector injector )
    {
        if ( beans.isEmpty() )
        {
            return Collections.emptyList();
        }
        final List<Entry<Q, T>> oldBeans = new ArrayList<Entry<Q, T>>();
        final List<Binding<T>> bindings = injector.findBindingsByType( key.getTypeLiteral() );
        for ( int i = 0; i < beans.size(); i++ )
        {
            if ( bindings.contains( beans.get( i ).binding ) )
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

    public synchronized List<Entry<Q, T>> clear()
    {
        @SuppressWarnings( "unchecked" )
        final List<Entry<Q, T>> oldBeans = (List) beans;
        beans = Collections.emptyList();
        defaultIndex = 0;
        exposed = false;
        return oldBeans;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Inserts a qualified bean entry for the given binding into this bean sequence.
     * 
     * @param binding The Guice binding
     * @return Qualified bean entry
     */
    private Entry<Q, T> insertQualifiedBean( final Binding<T> binding )
    {
        if ( exposed || beans.isEmpty() )
        {
            // swap in defensive copy so we don't disturb iterators
            beans = new ArrayList<DeferredBeanEntry<Q, T>>( beans );
            exposed = false;
        }
        final DeferredBeanEntry<Q, T> bean = new DeferredBeanEntry<Q, T>( binding );
        if ( BeanLocator.DEFAULT == bean.getKey() )
        {
            // defaults always go at the front
            beans.add( defaultIndex++, bean );
        }
        else
        {
            beans.add( bean );
        }
        return bean;
    }

    /**
     * Extracts a qualified bean entry from the given position in this bean sequence.
     * 
     * @param index The entry position
     * @return Qualified bean entry
     */
    private Entry<Q, T> extractQualifiedBean( final int index )
    {
        if ( exposed )
        {
            // swap in defensive copy so we don't disturb iterators
            beans = new ArrayList<DeferredBeanEntry<Q, T>>( beans );
            exposed = false;
        }
        final DeferredBeanEntry<Q, T> bean = beans.remove( index );
        if ( BeanLocator.DEFAULT == bean.getKey() )
        {
            defaultIndex--; // one less default
        }
        return bean;
    }

    private static boolean isVisible( final Binding<?> binding )
    {
        return false == binding.getSource() instanceof HiddenSource;
    }
}
