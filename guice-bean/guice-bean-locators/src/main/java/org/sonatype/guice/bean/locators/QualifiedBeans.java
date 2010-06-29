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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * {@link Iterable} sequence of qualified beans backed by bindings from one or more {@link Injector}s.
 */
class QualifiedBeans<Q extends Annotation, T>
    implements Iterable<QualifiedBean<Q, T>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Annotation DEFAULT_QUALIFIER = Names.named( "default" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Key<T> key;

    private List<QualifiedBean<Q, T>> beans = Collections.emptyList();

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

    public final synchronized Iterator<QualifiedBean<Q, T>> iterator()
    {
        exposed = true; // flag that someone is using the beans
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
        if ( key.hasAttributes() )
        {
            // optimization for looking up a specific (unique) qualified bean
            final Binding binding = injector.getBindings().get( normalize( key ) );
            if ( isVisible( null, binding ) )
            {
                return Collections.singletonList( insertQualifiedBean( binding ) );
            }
            return Collections.EMPTY_LIST;
        }

        final TypeLiteral<T> beanType = key.getTypeLiteral();
        final Class<? extends Annotation> qualifierType = key.getAnnotationType();

        // looking for any explicit default/qualified binding that implements this type
        final List<QualifiedBean<Q, T>> newBeans = new ArrayList<QualifiedBean<Q, T>>();
        for ( final Binding<T> binding : injector.findBindingsByType( beanType ) )
        {
            if ( isVisible( qualifierType, binding ) )
            {
                newBeans.add( insertQualifiedBean( binding ) );
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
        defaultIndex = 0;
        exposed = false;
        return oldBeans;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Inserts a qualified bean for the given binding into this bean sequence.
     * 
     * @param binding The Guice binding
     * @return Qualified bean
     */
    private QualifiedBean<Q, T> insertQualifiedBean( final Binding<T> binding )
    {
        if ( exposed || beans.isEmpty() )
        {
            // take defensive copy to avoid disturbing iterators
            beans = new ArrayList<QualifiedBean<Q, T>>( beans );
            exposed = false;
        }
        final QualifiedBean<Q, T> bean = new QualifiedBean<Q, T>( binding );
        if ( DEFAULT_QUALIFIER.equals( bean.getKey() ) )
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
     * Extracts a qualified bean from the given position in this bean sequence.
     * 
     * @param index The bean position
     * @return Qualified bean
     */
    private QualifiedBean<Q, T> extractQualifiedBean( final int index )
    {
        if ( exposed )
        {
            // take defensive copy to avoid disturbing iterators
            beans = new ArrayList<QualifiedBean<Q, T>>( beans );
            exposed = false;
        }
        final QualifiedBean<Q, T> bean = beans.remove( index );
        if ( DEFAULT_QUALIFIER.equals( bean.getKey() ) )
        {
            defaultIndex--; // one less default
        }
        return bean;
    }

    /**
     * Normalizes the given binding key by mapping the default qualifier to no qualifier.
     * 
     * @param key The binding key
     * @return Normalized key
     */
    private static <T> Key<T> normalize( final Key<T> key )
    {
        return DEFAULT_QUALIFIER.equals( key.getAnnotation() ) ? Key.get( key.getTypeLiteral() ) : key;
    }

    /**
     * Determines whether the given bean binding is visible to the {@link BeanLocator}.
     * 
     * @param qualifierType The expected qualifier type
     * @param binding The bean binding
     * @return {@code true} if the binding is visible; otherwise {@code false}
     */
    private static boolean isVisible( final Class<? extends Annotation> qualifierType, final Binding<?> binding )
    {
        if ( null == binding || binding.getSource() instanceof HiddenSource )
        {
            return false; // exclude hidden bindings (avoids recursion)
        }
        final Annotation qualifier = binding.getKey().getAnnotation();
        if ( DEFAULT_QUALIFIER.equals( qualifier ) )
        {
            return false; // exclude explicit @Named("default") beans
        }
        if ( null == qualifierType || qualifierType == Named.class && null == qualifier )
        {
            return true; // @Named search should also match defaults
        }
        return null != qualifier && qualifierType == qualifier.annotationType();
    }
}
