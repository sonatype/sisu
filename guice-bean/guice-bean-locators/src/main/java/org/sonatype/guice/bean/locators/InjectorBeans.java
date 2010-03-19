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

/**
 * Fixed {@link List} of qualified beans found by querying bindings from a single {@link Injector}.
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

        final Injector parent = injector.getParent();
        if ( null == parent )
        {
            try
            {
                add( new DeferredBeanEntry<Q, T>( null, injector.getProvider( key ) ) );
            }
            catch ( final RuntimeException e ) // NOPMD
            {
                // default binding may not exist
            }
        }

        final Class<? extends Annotation> qualifierType = key.getAnnotationType();
        if ( null == qualifierType )
        {
            return; // key only matches the default case, so we can stop here
        }

        for ( final Binding<T> binding : injector.findBindingsByType( key.getTypeLiteral() ) )
        {
            @SuppressWarnings( "unchecked" )
            final Q ann = (Q) binding.getKey().getAnnotation();
            if ( qualifierType.isInstance( ann ) )
            {
                add( new DeferredBeanEntry<Q, T>( ann, binding.getProvider() ) );
            }
            else if ( null == ann && null != parent )
            {
                // default child bindings should appear before qualified bindings
                add( 0, new DeferredBeanEntry<Q, T>( null, binding.getProvider() ) );
            }
        }

        trimToSize(); // minimize memory usage
    }
}
