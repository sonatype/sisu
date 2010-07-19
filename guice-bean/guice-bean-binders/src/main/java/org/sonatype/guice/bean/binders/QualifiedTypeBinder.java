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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Typed;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.NamedMediatorAdapter;
import org.sonatype.guice.bean.reflect.TypeParameters;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.inject.Mediator;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * {@link QualifiedTypeListener} that installs {@link Module}s, registers {@link Mediator}s, and binds types.
 */
public final class QualifiedTypeBinder
    implements QualifiedTypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder binder;

    private BeanListener beanListener;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedTypeBinder( final Binder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void hear( final Annotation qualifier, final Class qualifiedType, final Object source )
    {
        if ( Module.class.isAssignableFrom( qualifiedType ) )
        {
            installModule( qualifiedType );
        }
        else if ( Mediator.class.isAssignableFrom( qualifiedType ) )
        {
            registerMediator( qualifiedType );
        }
        else
        {
            bindQualifiedType( qualifiedType, null != source ? binder.withSource( source ) : binder );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Installs an instance of the given {@link Module}.
     * 
     * @param moduleType The module type
     */
    private void installModule( final Class<Module> moduleType )
    {
        final Module module = newInstance( moduleType );
        if ( null != module )
        {
            binder.install( module );
        }
    }

    /**
     * Registers an instance of the given {@link Mediator} using its generic type parameters as configuration.
     * 
     * @param mediatorType The mediator type
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private <Q, T, W> void registerMediator( final Class<Mediator<Q, T, W>> mediatorType )
    {
        final TypeLiteral<?>[] params = getSuperTypeParameters( mediatorType, Mediator.class );
        if ( params.length != 3 )
        {
            binder.addError( mediatorType + " has wrong number of type arguments" );
        }
        else
        {
            Mediator<Q, T, W> mediator = newInstance( mediatorType );
            if ( null != mediator )
            {
                Class qualifierType = params[0].getRawType();
                if ( String.class == qualifierType )
                {
                    // wrap delegating mediator around the original
                    mediator = new NamedMediatorAdapter( mediator );
                    qualifierType = Named.class;
                }
                mediate( Key.get( params[1], qualifierType ), mediator, (Class<W>) params[2].getRawType() );
            }
        }
    }

    /**
     * Uses the given mediator to mediate updates between the {@link BeanLocator} and associated watchers.
     * 
     * @param key The watched key
     * @param mediator The bean mediator
     * @param watcherType The watcher type
     */
    private <Q, T, W> void mediate( final Key<T> key, final Mediator<Q, T, W> mediator, final Class<W> watcherType )
    {
        if ( null == beanListener )
        {
            beanListener = new BeanListener();
            binder.bindListener( Matchers.any(), beanListener );
            binder.requestInjection( beanListener );
        }
        beanListener.mediate( key, mediator, watcherType );
    }

    /**
     * Binds the given qualified type using a binding key determined by common-use heuristics.
     * 
     * @param qualifiedType The qualified type
     * @param binder The type binder
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private static void bindQualifiedType( final Class<?> qualifiedType, final Binder binder )
    {
        final boolean isEagerSingleton = qualifiedType.isAnnotationPresent( EagerSingleton.class );
        if ( isEagerSingleton )
        {
            binder.bind( qualifiedType ).asEagerSingleton();
        }
        final Class bindingType = getBindingType( qualifiedType );
        final Annotation bindingQualifier = getBindingQualifier( bindingType, qualifiedType );
        if ( null != bindingQualifier )
        {
            binder.bind( Key.get( bindingType, bindingQualifier ) ).to( qualifiedType );
        }
        else if ( bindingType != qualifiedType )
        {
            binder.bind( bindingType ).to( qualifiedType );
        }
        else if ( !isEagerSingleton )
        {
            binder.bind( qualifiedType );
        }
    }

    /**
     * Attempts to create a new instance of the given type.
     * 
     * @param type The instance type
     * @return New instance; {@code null} if the instance couldn't be created
     */
    private <T> T newInstance( final Class<T> type )
    {
        try
        {
            // slightly roundabout approach, but it might be private
            final Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible( true );
            return ctor.newInstance();
        }
        catch ( final Throwable e )
        {
            binder.addError( "Error creating instance of: " + type + " reason: " + e );
            return null;
        }
    }

    /**
     * Resolves the type parameters of a super type based on the given concrete type.
     * 
     * @param type The concrete type
     * @param superType The generic super type
     * @return Resolved super type parameters
     */
    private static TypeLiteral<?>[] getSuperTypeParameters( final Class<?> type, final Class<?> superType )
    {
        return TypeParameters.get( TypeLiteral.get( type ).getSupertype( superType ) );
    }

    /**
     * Determines the binding type to be used for the given qualified type.
     * 
     * @param qualifiedType The qualified type
     * @return Selected binding type
     */
    @SuppressWarnings( "rawtypes" )
    private static Class getBindingType( final Class qualifiedType )
    {
        if ( qualifiedType.isInterface() )
        {
            return qualifiedType;
        }
        final List<Class> hierarchy = new ArrayList<Class>();
        for ( Class<?> clazz = qualifiedType; clazz != Object.class; clazz = clazz.getSuperclass() )
        {
            final Typed typed = clazz.getAnnotation( Typed.class );
            if ( null != typed && typed.value().length > 0 )
            {
                return typed.value()[0];
            }
            hierarchy.add( clazz );
            Collections.addAll( hierarchy, clazz.getInterfaces() );
        }
        final String qualifiedName = qualifiedType.getName();
        for ( int n = hierarchy.size() - 1; n > 0; n-- )
        {
            final Class clazz = hierarchy.get( n );
            if ( qualifiedName.endsWith( clazz.getSimpleName() ) )
            {
                return clazz;
            }
        }
        return hierarchy.size() > 1 ? hierarchy.get( 1 ) : qualifiedType;
    }

    private static Annotation getBindingQualifier( final Class<?> bindingType, final Class<?> qualifiedType )
    {
        Named named = qualifiedType.getAnnotation( Named.class );
        final javax.inject.Named jsr330 = qualifiedType.getAnnotation( javax.inject.Named.class );
        if ( null != jsr330 )
        {
            named = Names.named( jsr330.value() );
        }
        if ( null == named || named.value().length() == 0 )
        {
            final String qualifiedName = qualifiedType.getSimpleName();
            if ( !qualifiedName.startsWith( "Default" ) || !qualifiedName.endsWith( bindingType.getSimpleName() ) )
            {
                return Names.named( qualifiedType.getName() ); // implicit name
            }
            return null; // implicit default
        }
        if ( "default".equalsIgnoreCase( named.value() ) )
        {
            return null; // explicit default
        }
        return named; // explicit name
    }
}
