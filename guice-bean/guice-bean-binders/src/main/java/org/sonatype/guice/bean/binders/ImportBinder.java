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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.reflect.TypeParameters;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Adds {@link BeanLocator}-backed bindings for non-local bean dependencies.
 */
final class ImportBinder
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static Named WILDCARD_NAME = Names.named( "" );

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
            if ( Map.class == clazz )
            {
                bindMapImport( key );
            }
            else if ( List.class == clazz )
            {
                bindListImport( key );
            }
            else
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
        if ( 2 == parameters.length )
        {
            final Class qualifierType = parameters[0].getRawType();
            final Type bindingType = parameters[1].getType();
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
    @SuppressWarnings( "unchecked" )
    private void bindListImport( final Key key )
    {
        // this assumes that imported lists should just contain @Named beans
        final TypeLiteral[] parameters = TypeParameters.get( key.getTypeLiteral() );
        if ( 1 == parameters.length )
        {
            final Type bindingType = parameters[0].getType();
            binder.bind( key ).toProvider( new BeanListProvider( Key.get( bindingType, Named.class ) ) );
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
        if ( WILDCARD_NAME.equals( key.getAnnotation() ) )
        {
            // special case for wildcard @Named dependencies: match any @Named bean regardless of actual name
            binder.bind( key ).toProvider( new BeanProvider( Key.get( key.getTypeLiteral(), Named.class ) ) );
        }
        else
        {
            binder.bind( key ).toProvider( new BeanProvider( key ) );
        }
    }
}
