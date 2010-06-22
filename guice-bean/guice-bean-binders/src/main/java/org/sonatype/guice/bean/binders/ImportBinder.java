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
package org.sonatype.guice.bean.binders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.locators.BeanLocator;
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
final class ImportBinder
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Map<String, String> DEFAULT_PROPERTIES = Collections.emptyMap();

    private static final String[] DEFAULT_ARGUMENTS = {};

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ImportBinder( final Binder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Registers {@link BeanLocator}-backed bindings for the given set of non-local dependencies.
     * 
     * @param importedKeys Set of non-local dependencies
     */
    public void bind( final Set<Key<?>> importedKeys )
    {
        for ( final Key<?> key : importedKeys )
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
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Adds an imported {@link Map} binding; uses the generic type parameters to determine the search details.
     * 
     * @param key The dependency key
     */
    @SuppressWarnings( "unchecked" )
    private void bindMapImport( final Key key )
    {
        final TypeLiteral[] parameters = TypeParameters.get( key.getTypeLiteral() );
        if ( 2 == parameters.length && null == key.getAnnotation() )
        {
            final Class qualifierType = parameters[0].getRawType();
            final Type bindingType = parameters[1].getType();
            if ( qualifierType == String.class )
            {
                binder.bind( key ).toProvider( new NamedBeanMapProvider( bindingType ) );
            }
            else if ( qualifierType == Annotation.class )
            {
                binder.bind( key ).toProvider( new BeanMapProvider( Key.get( bindingType ) ) );
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
    @SuppressWarnings( "unchecked" )
    private void bindListImport( final Key key )
    {
        final TypeLiteral[] parameters = TypeParameters.get( key.getTypeLiteral() );
        if ( 1 == parameters.length && null == key.getAnnotation() )
        {
            final Type bindingType = parameters[0].getType();
            binder.bind( key ).toProvider( new BeanListProvider( Key.get( bindingType ) ) );
        }
    }

    /**
     * Adds an imported bean binding; uses the type and {@link Qualifier} annotation to determine the search details.
     * 
     * @param key The dependency key
     */
    @SuppressWarnings( "unchecked" )
    private void bindBeanImport( final Key key )
    {
        final Annotation qualifier = key.getAnnotation();
        final String name = qualifier instanceof Named ? ( (Named) qualifier ).value() : "CUSTOM";
        if ( name.contains( "${" ) )
        {
            binder.bind( key ).toProvider( new PlaceholderBeanProvider( key ) );
        }
        else if ( name.length() == 0 )
        {
            // special case for wildcard @Named dependencies: match any @Named bean regardless of actual name
            binder.bind( key ).toProvider( new BeanProvider( Key.get( key.getTypeLiteral(), Named.class ) ) );
        }
        else if ( !isImplicit( key ) )
        {
            // avoid messing with any unqualified implicit bindings
            binder.bind( key ).toProvider( new BeanProvider( key ) );
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
