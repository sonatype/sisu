/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.binders;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.HiddenBinding;
import org.sonatype.guice.bean.reflect.TypeParameters;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

/**
 * Adds {@link BeanLocator}-backed bindings for non-local bean dependencies.
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
final class LocatorWiring
    implements Wiring
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final HiddenBinding HIDDEN_SOURCE = new HiddenBinding()
    {
        @Override
        public String toString()
        {
            return LocatorWiring.class.getName();
        }
    };

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LocatorWiring( final Binder binder )
    {
        this.binder = binder.withSource( HIDDEN_SOURCE );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean wire( final Key<?> key )
    {
        final Class<?> clazz = key.getTypeLiteral().getRawType();
        if ( Map.class == clazz )
        {
            bindMapImport( key );
        }
        else if ( List.class == clazz || Iterable.class == clazz )
        {
            bindListImport( key );
        }
        else if ( !isRestricted( clazz ) )
        {
            bindBeanImport( key );
        }
        return true;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Adds an imported {@link Map} binding; uses the generic type parameters to determine the search details.
     * 
     * @param key The dependency key
     */
    private void bindMapImport( final Key<?> key )
    {
        final TypeLiteral<?>[] parameters = TypeParameters.get( key.getTypeLiteral() );
        if ( 2 == parameters.length && null == key.getAnnotation() )
        {
            final Class qualifierType = parameters[0].getRawType();
            if ( String.class == qualifierType )
            {
                binder.bind( key ).toProvider( new NamedBeanMapProvider( parameters[1] ) );
            }
            else if ( qualifierType.isAnnotationPresent( Qualifier.class ) )
            {
                binder.bind( key ).toProvider( new BeanMapProvider( Key.get( parameters[1], qualifierType ) ) );
            }
        }
    }

    /**
     * Adds an imported {@link List} binding; uses the generic type parameters to determine the search details.
     * 
     * @param key The dependency key
     */
    private void bindListImport( final Key<?> key )
    {
        final TypeLiteral<?>[] parameters = TypeParameters.get( key.getTypeLiteral() );
        if ( 1 == parameters.length && null == key.getAnnotation() )
        {
            binder.bind( key ).toProvider( new BeanListProvider( Key.get( parameters[0] ) ) );
        }
    }

    /**
     * Adds an imported bean binding; uses the type and {@link Qualifier} annotation to determine the search details.
     * 
     * @param key The dependency key
     */
    private <T> void bindBeanImport( final Key<T> key )
    {
        final Annotation qualifier = key.getAnnotation();
        if ( qualifier instanceof Named )
        {
            if ( ( (Named) qualifier ).value().length() == 0 )
            {
                // special case for wildcard @Named dependencies: match any @Named bean regardless of actual name
                binder.bind( key ).toProvider( new BeanProvider<T>( Key.get( key.getTypeLiteral(), Named.class ) ) );
            }
            else
            {
                binder.bind( key ).toProvider( new PlaceholderBeanProvider<T>( key ) );
            }
        }
        else
        {
            binder.bind( key ).toProvider( new BeanProvider<T>( key ) );
        }
    }

    /**
     * Determines whether the given type is restricted and therefore can never be overridden by the import binder.
     * 
     * @param clazz The binding type
     * @return {@code true} if the given type is restricted; otherwise {@code false}
     */
    private static boolean isRestricted( final Class<?> clazz )
    {
        return clazz.getName().equals( "org.slf4j.Logger" ) || BeanLocator.class.isAssignableFrom( clazz );
    }
}
