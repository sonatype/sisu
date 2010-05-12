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
import java.util.List;
import java.util.Map.Entry;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Fixed {@link List} of qualified beans found by querying explicit bindings from a single {@link Injector}.
 */
final class InjectorBeans<Q extends Annotation, T>
    extends ArrayList<Entry<Q, T>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final transient Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    InjectorBeans( final Injector injector, final Key<T> key )
    {
        this.injector = injector;
        if ( key.hasAttributes() )
        {
            addQualifiedBean( key.getAnnotation(), injector.getBindings().get( key ) );
        }
        else
        {
            addQualifiedBeans( key.getAnnotationType(), key.getTypeLiteral() );
        }
        trimToSize(); // minimize memory usage
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Adds the specific qualified bean to the collection.
     * 
     * @param qualifier The bean qualifier
     * @param provider The bean provider
     */
    @SuppressWarnings( "unchecked" )
    private void addQualifiedBean( final Annotation qualifier, final Binding binding )
    {
        add( new DeferredBeanEntry( qualifier, binding.getProvider() ) );
    }

    /**
     * Adds all beans of the given type whose qualifiers match the given qualifier type.
     * 
     * @param qualifierType The qualifier type
     * @param beanType The bean type
     */
    private void addQualifiedBeans( final Class<? extends Annotation> qualifierType, final TypeLiteral<T> beanType )
    {
        for ( final Binding<T> binding : injector.findBindingsByType( beanType ) )
        {
            final Annotation ann = binding.getKey().getAnnotation();
            if ( null == qualifierType || qualifierType.isInstance( ann ) )
            {
                addQualifiedBean( ann, binding );
            }
        }
    }
}
