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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.HiddenBinding;
import org.sonatype.guice.bean.reflect.TypeParameters;

import com.google.inject.Binder;
import com.google.inject.ImplementedBy;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
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

    private static final String[] DEFAULT_ARGUMENTS = {};

    private static final Map<String, String> DEFAULT_PROPERTIES;

    static
    {
        Map defaultProperties;
        try
        {
            defaultProperties = System.getProperties();
        }
        catch ( final Throwable e )
        {
            defaultProperties = new HashMap<String, String>();
        }
        DEFAULT_PROPERTIES = defaultProperties;
    }

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
        if ( ParameterKeys.PROPERTIES.equals( key ) )
        {
            binder.bind( ParameterKeys.PROPERTIES ).toInstance( DEFAULT_PROPERTIES );
        }
        else if ( Map.class == clazz )
        {
            bindMapImport( key );
        }
        else if ( List.class == clazz )
        {
            bindListImport( key );
        }
        else if ( ParameterKeys.ARGUMENTS.equals( key ) )
        {
            binder.bind( ParameterKeys.ARGUMENTS ).toInstance( DEFAULT_ARGUMENTS );
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
            final Class bindingType = parameters[1].getRawType();
            if ( qualifierType == String.class )
            {
                binder.bind( key ).toProvider( new NamedBeanMapProvider( bindingType ) );
            }
            else if ( qualifierType.isAnnotationPresent( Qualifier.class ) )
            {
                binder.bind( key ).toProvider( new BeanMapProvider( Key.get( bindingType, qualifierType ) ) );
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
            final Class bindingType = parameters[0].getRawType();
            binder.bind( key ).toProvider( new BeanListProvider( Key.get( bindingType ) ) );
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
        final String name = qualifier instanceof Named ? ( (Named) qualifier ).value() : "CUSTOM";
        final Class bindingType = key.getTypeLiteral().getRawType();
        if ( name.contains( "${" ) )
        {
            binder.bind( key ).toProvider( new PlaceholderBeanProvider<T>( key.ofType( bindingType ) ) );
        }
        else if ( name.length() == 0 )
        {
            // special case for wildcard @Named dependencies: match any @Named bean regardless of name
            binder.bind( key ).toProvider( new BeanProvider<T>( Key.get( bindingType, Named.class ) ) );
        }
        else if ( !isImplicit( key ) )
        {
            // avoid messing with any unqualified implicit bindings
            binder.bind( key ).toProvider( new BeanProvider<T>( key.ofType( bindingType ) ) );
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
        return clazz.getName().startsWith( "com.google.inject" );
    }

    /**
     * Determines whether the given key is an implicit binding which shouldn't be overridden by the import binder.
     * 
     * @param key The binding key
     * @return {@code true} if the implementation of the given type is implicit; otherwise {@code false}
     */
    private static boolean isImplicit( final Key<?> key )
    {
        if ( null != key.getAnnotationType() )
        {
            return false; // qualified key, so implicit rules aren't applied
        }
        final Class<?> clazz = key.getTypeLiteral().getRawType();
        if ( clazz.getName().equals( "org.slf4j.Logger" ) )
        {
            return true; // automatically handled by our patched Guice build
        }
        if ( clazz.isInterface() )
        {
            return clazz.isAnnotationPresent( ImplementedBy.class ) || clazz.isAnnotationPresent( ProvidedBy.class );
        }
        return !Modifier.isAbstract( clazz.getModifiers() ); // concrete types are implicit
    }
}
