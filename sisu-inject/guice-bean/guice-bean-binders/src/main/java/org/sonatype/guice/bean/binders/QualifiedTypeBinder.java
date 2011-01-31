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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Typed;

import org.sonatype.guice.bean.locators.BeanLocator;
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
    private void registerMediator( final Class<Mediator> mediatorType )
    {
        final TypeLiteral<?>[] params = getSuperTypeParameters( mediatorType, Mediator.class );
        if ( params.length != 3 )
        {
            binder.addError( mediatorType + " has wrong number of type arguments" );
        }
        else
        {
            final Mediator mediator = newInstance( mediatorType );
            if ( null != mediator )
            {
                mediate( Key.get( params[1], (Class) params[0].getRawType() ), mediator, params[2].getRawType() );
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
    @SuppressWarnings( "rawtypes" )
    private void mediate( final Key key, final Mediator mediator, final Class watcherType )
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

        if ( qualifiedType.isInterface() )
        {
            return;
        }

        final Named fullName = Names.named( qualifiedType.getName() );
        final Named customName = getCustomName( qualifiedType );

        binder.bind( Key.get( Object.class, fullName ) ).to( qualifiedType );

        for ( final Class bindingType : getBindingTypes( qualifiedType ) )
        {
            if ( bindingType != qualifiedType )
            {
                final Named bindingName = getBindingName( bindingType, fullName, customName );
                if ( null != bindingName )
                {
                    binder.bind( Key.get( bindingType, bindingName ) ).to( qualifiedType );
                }
                else
                {
                    binder.bind( bindingType ).to( qualifiedType );
                }
            }
            else if ( !isEagerSingleton )
            {
                binder.bind( qualifiedType );
            }
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
     * Determines the binding types to be used for the given qualified type.
     * 
     * @param qualifiedType The qualified type
     * @return Selected binding types
     */
    private static List<Class<?>> getBindingTypes( final Class<?> qualifiedType )
    {
        final Typed typed = qualifiedType.getAnnotation( Typed.class );
        if ( null != typed )
        {
            return Arrays.asList( typed.value() );
        }

        final String simpleName = qualifiedType.getSimpleName();
        final Class<?> extendedClazz = qualifiedType.getSuperclass();

        final List<Class<?>> types = new ArrayList<Class<?>>();
        for ( Class<?> clazz = qualifiedType; clazz != Object.class; clazz = clazz.getSuperclass() )
        {
            if ( clazz == extendedClazz || simpleName.endsWith( clazz.getSimpleName() ) )
            {
                types.add( clazz );
            }
            for ( final Class<?> iFace : clazz.getInterfaces() )
            {
                if ( clazz == qualifiedType || simpleName.endsWith( iFace.getSimpleName() ) )
                {
                    types.add( iFace );
                }
            }
        }
        return types;
    }

    private static Named getCustomName( final Class<?> qualifiedType )
    {
        final javax.inject.Named jsr330 = qualifiedType.getAnnotation( javax.inject.Named.class );
        if ( null != jsr330 )
        {
            return Names.named( jsr330.value() );
        }
        return qualifiedType.getAnnotation( Named.class );
    }

    private static Named getBindingName( final Class<?> bindingType, final Named fullName, final Named customName )
    {
        if ( Object.class == bindingType )
        {
            return fullName;
        }
        if ( null == customName || customName.value().length() == 0 )
        {
            final String fqn = fullName.value();
            if ( fqn.contains( "Default" ) && fqn.endsWith( bindingType.getSimpleName() ) )
            {
                return null;
            }
            return fullName;
        }
        if ( "default".equalsIgnoreCase( customName.value() ) )
        {
            return null;
        }
        return customName;
    }
}
