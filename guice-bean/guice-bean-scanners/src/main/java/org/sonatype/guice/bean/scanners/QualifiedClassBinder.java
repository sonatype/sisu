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
package org.sonatype.guice.bean.scanners;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.reflect.DeclaredMembers;
import org.sonatype.guice.bean.reflect.Generics;
import org.sonatype.inject.Mediator;
import org.sonatype.inject.NamedMediator;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Types;

/**
 * Auto-wires the qualified bean according to the attached {@link Qualifier} metadata.
 */
@SuppressWarnings( "unchecked" )
final class QualifiedClassBinder
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Named DEFAULT_NAMED = Names.named( "default" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Key, Class> bindings = new LinkedHashMap<Key, Class>();

    private final Set<TypeLiteral> boundTypes = new HashSet<TypeLiteral>();

    private final Set<Class> mediatorTypes = new LinkedHashSet<Class>();

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    QualifiedClassBinder( final Binder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    /**
     * Auto-binds the given qualified types and detects any use of qualified bean sequences.
     * 
     * @param qualifiedTypes The list of qualified types
     */
    void bind( final List<Class<?>> qualifiedTypes )
    {
        for ( int i = 0, size = qualifiedTypes.size(); i < size; i++ )
        {
            final Class clazz = qualifiedTypes.get( i );
            if ( Module.class.isAssignableFrom( clazz ) )
            {
                install( clazz ); // add custom Guice bindings
            }
            else
            {
                bind( clazz ); // record qualified bean type
            }
        }

        // apply the final collection of recorded bindings
        for ( final Entry<Key, Class> e : bindings.entrySet() )
        {
            binder.bind( e.getKey() ).to( e.getValue() );
        }

        // mediation requires that we listen for instances
        for ( final Class mediatorType : mediatorTypes )
        {
            if ( NamedMediator.class.isAssignableFrom( mediatorType ) )
            {
                final NamedWatcherListener listener = new NamedWatcherListener( mediatorType );
                binder.bindListener( listener, listener );
            }
            else if ( Mediator.class.isAssignableFrom( mediatorType ) )
            {
                final WatcherListener listener = new WatcherListener( mediatorType );
                binder.bindListener( listener, listener );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Install the given custom bindings into the current module.
     * 
     * @param clazz The module class
     */
    private void install( final Class<Module> clazz )
    {
        try
        {
            final Constructor<Module> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible( true );
            binder.install( ctor.newInstance() );
        }
        catch ( final InvocationTargetException e )
        {
            binder.addError( e.getTargetException() );
        }
        catch ( final Throwable e )
        {
            binder.addError( e );
        }
    }

    /**
     * Records a binding for the given qualified bean type.
     * 
     * @param clazz The qualified bean type
     */
    private void bind( final Class clazz )
    {
        final Class keyType = getKeyType( clazz );
        final Annotation qualifier = normalize( getQualifier( clazz ), clazz );
        final Key qualifiedKey = Key.get( keyType, qualifier );

        // go-ahead and bind defaults now
        if ( qualifier == DEFAULT_NAMED )
        {
            if ( keyType != clazz )
            {
                binder.bind( keyType ).to( qualifiedKey );
            }
            else
            {
                binder.bind( keyType ); // API == Impl
            }
            binder.bind( qualifiedKey ).to( clazz );
        }
        else
        {
            // other bindings can wait until later
            bindings.put( qualifiedKey, clazz );
        }

        // check fields for qualified sequences
        bindQualifiedBeanCollections( clazz );

        if ( NamedMediator.class.isAssignableFrom( clazz ) || Mediator.class.isAssignableFrom( clazz ) )
        {
            mediatorTypes.add( clazz );
        }
    }

    /**
     * Normalizes the given qualifier.
     * 
     * @param qualifier The bean qualifier
     * @param clazz The bean type
     * @return Normalized qualifier
     */
    private static Annotation normalize( final Annotation qualifier, final Class clazz )
    {
        final String hint;
        if ( qualifier instanceof javax.inject.Named )
        {
            hint = ( (javax.inject.Named) qualifier ).value();
        }
        else if ( qualifier instanceof Named )
        {
            hint = ( (Named) qualifier ).value();
        }
        else
        {
            return qualifier;
        }

        if ( hint.length() == 0 )
        {
            // Assume Default* types are default implementations
            if ( clazz.getSimpleName().startsWith( "Default" ) )
            {
                return DEFAULT_NAMED;
            }
            return Names.named( clazz.getName() );
        }

        if ( "default".equalsIgnoreCase( hint ) )
        {
            return DEFAULT_NAMED;
        }

        return qualifier;
    }

    /**
     * Searches the given type's hierarchy for the qualifier annotation.
     * 
     * @param clazz The qualified type
     * @return Qualifier annotation
     */
    private static Annotation getQualifier( final Class clazz )
    {
        for ( final Annotation ann : clazz.getAnnotations() )
        {
            // pick first annotation marked with a @Qualifier meta-annotation
            if ( ann.annotationType().isAnnotationPresent( Qualifier.class ) )
            {
                return ann;
            }
        }
        throw new IllegalArgumentException( clazz + " has no @Qualifier annotation" );
    }

    /**
     * Searches the given type's hierarchy for the key binding type.
     * 
     * @param clazz The qualified type
     * @return Key binding type
     */
    private Class getKeyType( final Class<?> clazz )
    {
        final Typed typed = clazz.getAnnotation( Typed.class );
        final Class[] interfaces = null != typed ? typed.value() : clazz.getInterfaces();
        if ( interfaces.length == 1 )
        {
            return interfaces[0];
        }

        // disallow ambiguous bindings
        if ( interfaces.length > 1 )
        {
            throw new IllegalArgumentException( clazz + " has multiple interfaces, use @Typed to remove ambiguity" );
        }

        final Class superClazz = clazz.getSuperclass();
        if ( superClazz.getName().startsWith( "java" ) )
        {
            return Object.class == superClazz ? clazz : superClazz;
        }

        return getKeyType( superClazz );
    }

    /**
     * Scans the given bean type for any use of qualified collections and adds the necessary bindings for them.
     * 
     * @param clazz The bean type
     */
    private void bindQualifiedBeanCollections( final Class clazz )
    {
        for ( final Member member : new DeclaredMembers( clazz ) )
        {
            if ( member instanceof Field && ( (AnnotatedElement) member ).isAnnotationPresent( Inject.class ) )
            {
                final TypeLiteral fieldType = TypeLiteral.get( ( (Field) member ).getGenericType() );
                if ( !boundTypes.add( fieldType ) )
                {
                    continue; // already handled
                }
                else if ( fieldType.getRawType() == List.class )
                {
                    bindQualifiedList( fieldType );
                }
                else if ( fieldType.getRawType() == Map.class )
                {
                    bindQualifiedMap( fieldType );
                }
            }
        }
    }

    /**
     * Binds the given qualified list type to a {@link Provider} backed by the {@link BeanLocator}.
     * 
     * @param listType The list type
     */
    private void bindQualifiedList( final TypeLiteral listType )
    {
        final Type beanType = Generics.typeArgument( listType, 0 ).getType();
        final Type providerType = Types.newParameterizedType( ListProvider.class, beanType );
        binder.bind( listType ).toProvider( Key.get( providerType ) );
    }

    /**
     * Binds the given qualified map type to a {@link Provider} backed by the {@link BeanLocator}.
     * 
     * @param mapType The map type
     */
    private void bindQualifiedMap( final TypeLiteral mapType )
    {
        final Type qualifierType = Generics.typeArgument( mapType, 0 ).getType();
        final Type beanType = Generics.typeArgument( mapType, 1 ).getType();
        final Type providerType;
        if ( qualifierType == String.class )
        {
            providerType = Types.newParameterizedType( MapHintProvider.class, beanType );
        }
        else
        {
            providerType = Types.newParameterizedType( MapProvider.class, qualifierType, beanType );
        }
        binder.bind( mapType ).toProvider( Key.get( providerType ) );
    }
}
