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
import java.lang.reflect.Modifier;

import javax.enterprise.inject.Typed;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.WildcardKey;
import org.sonatype.guice.bean.reflect.TypeParameters;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.inject.Mediator;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
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
    // Constants
    // ----------------------------------------------------------------------

    private static final Named DEFAULT_NAME = Names.named( "default" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder rootBinder;

    private BeanListener beanListener;

    private Object currentSource;

    private Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedTypeBinder( final Binder binder )
    {
        rootBinder = binder;
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void hear( final Annotation qualifier, final Class qualifiedType, final Object source )
    {
        if ( currentSource != source )
        {
            if ( null != source )
            {
                binder = rootBinder.withSource( source );
                currentSource = source;
            }
            else
            {
                binder = rootBinder;
                currentSource = null;
            }
        }

        if ( ( qualifiedType.getModifiers() & ( Modifier.INTERFACE | Modifier.ABSTRACT ) ) != 0 )
        {
            return;
        }
        else if ( Module.class.isAssignableFrom( qualifiedType ) )
        {
            installModule( qualifiedType );
        }
        else if ( Mediator.class.isAssignableFrom( qualifiedType ) )
        {
            registerMediator( qualifiedType );
        }
        else if ( javax.inject.Provider.class.isAssignableFrom( qualifiedType ) )
        {
            bindProviderType( qualifiedType );
        }
        else
        {
            bindQualifiedType( qualifiedType );
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
     * Binds the given provider type using a binding key determined by common-use heuristics.
     * 
     * @param providerType The provider type
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void bindProviderType( final Class<?> providerType )
    {
        final TypeLiteral[] params = getSuperTypeParameters( providerType, javax.inject.Provider.class );
        if ( params.length != 1 )
        {
            binder.addError( providerType + " has wrong number of type arguments" );
        }
        else
        {
            final Key key = Key.get( params[0], getBindingName( providerType ) );
            final ScopedBindingBuilder sbb = binder.bind( key ).toProvider( providerType );
            if ( providerType.isAnnotationPresent( EagerSingleton.class ) )
            {
                sbb.asEagerSingleton();
            }
            else if ( providerType.isAnnotationPresent( javax.inject.Singleton.class )
                || providerType.isAnnotationPresent( com.google.inject.Singleton.class ) )
            {
                sbb.in( Scopes.SINGLETON );
            }

            final Typed typed = providerType.getAnnotation( Typed.class );
            if ( null != typed )
            {
                for ( final Class bindingType : typed.value() )
                {
                    binder.bind( key.ofType( bindingType ) ).to( key );
                }
            }
        }
    }

    /**
     * Binds the given qualified type using a binding key determined by common-use heuristics.
     * 
     * @param qualifiedType The qualified type
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void bindQualifiedType( final Class<?> qualifiedType )
    {
        final ScopedBindingBuilder sbb = binder.bind( qualifiedType );
        if ( qualifiedType.isAnnotationPresent( EagerSingleton.class ) )
        {
            sbb.asEagerSingleton();
        }

        final Named bindingName = getBindingName( qualifiedType );
        final Typed typed = qualifiedType.getAnnotation( Typed.class );
        if ( null != typed )
        {
            Class<?>[] interfaces = typed.value();
            if ( interfaces.length == 0 )
            {
                interfaces = qualifiedType.getInterfaces();
            }
            for ( final Class bindingType : interfaces )
            {
                binder.bind( Key.get( bindingType, bindingName ) ).to( qualifiedType );
            }
        }
        else
        {
            binder.bind( new WildcardKey( qualifiedType, bindingName ) ).to( qualifiedType );
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

    private static Named getBindingName( final Class<?> qualifiedType )
    {
        final javax.inject.Named jsr330 = qualifiedType.getAnnotation( javax.inject.Named.class );
        if ( null != jsr330 )
        {
            final String name = jsr330.value();
            if ( name.length() > 0 )
            {
                return "default".equals( name ) ? DEFAULT_NAME : Names.named( name );
            }
        }
        else
        {
            final Named guice = qualifiedType.getAnnotation( Named.class );
            if ( null != guice && guice.value().length() > 0 )
            {
                return guice;
            }
        }

        if ( qualifiedType.getSimpleName().startsWith( "Default" ) )
        {
            return DEFAULT_NAME;
        }

        return Names.named( qualifiedType.getName() );
    }
}
