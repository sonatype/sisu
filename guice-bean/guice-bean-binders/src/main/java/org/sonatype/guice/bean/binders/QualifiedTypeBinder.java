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

import javax.enterprise.inject.Typed;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.NamedBeanMediatorAdapter;
import org.sonatype.guice.bean.reflect.TypeParameters;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.inject.BeanMediator;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.inject.NamedBeanMediator;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * {@link QualifiedTypeListener} that installs {@link Module}s, registers {@link BeanMediator}s, and binds types.
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

    @SuppressWarnings( "unchecked" )
    public void hear( final Annotation qualifier, final Class qualifiedType )
    {
        if ( Module.class.isAssignableFrom( qualifiedType ) )
        {
            installModule( qualifiedType );
        }
        else if ( NamedBeanMediator.class.isAssignableFrom( qualifiedType ) )
        {
            registerNamedMediator( qualifiedType );
        }
        else if ( BeanMediator.class.isAssignableFrom( qualifiedType ) )
        {
            registerMediator( qualifiedType );
        }
        else
        {
            bindQualifiedType( qualifier, qualifiedType );
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
     * Registers an instance of the given {@link BeanMediator} using its generic type parameters as configuration.
     * 
     * @param mediatorType The mediator type
     */
    @SuppressWarnings( "unchecked" )
    private void registerMediator( final Class<BeanMediator> mediatorType )
    {
        final TypeLiteral[] params = getSuperTypeParameters( mediatorType, BeanMediator.class );
        if ( params.length != 3 )
        {
            binder.addError( mediatorType + " has wrong number of type arguments" );
        }
        else
        {
            final BeanMediator mediator = newInstance( mediatorType );
            if ( null != mediator )
            {
                mediate( Key.get( params[1], params[0].getRawType() ), mediator, params[2].getRawType() );
            }
        }
    }

    /**
     * Registers an instance of the given {@link NamedBeanMediator} using its generic type parameters as configuration.
     * 
     * @param mediatorType The mediator type
     */
    @SuppressWarnings( "unchecked" )
    private void registerNamedMediator( final Class<NamedBeanMediator> mediatorType )
    {
        final TypeLiteral[] params = getSuperTypeParameters( mediatorType, NamedBeanMediator.class );
        if ( params.length != 2 )
        {
            binder.addError( mediatorType + " has wrong number of type arguments" );
        }
        else
        {
            final NamedBeanMediator namedMediator = newInstance( mediatorType );
            if ( null != namedMediator )
            {
                final BeanMediator mediator = new NamedBeanMediatorAdapter( namedMediator );
                mediate( Key.get( params[0], Named.class ), mediator, params[1].getRawType() );
            }
        }
    }

    /**
     * Uses the given mediator to mediate updates between the {@link BeanLocator} and associated watchers.
     * 
     * @param watchedKey The watched key
     * @param mediator The bean mediator
     * @param watcherType The watcher type
     */
    @SuppressWarnings( "unchecked" )
    private void mediate( final Key watchedKey, final BeanMediator mediator, final Class watcherType )
    {
        if ( null == beanListener )
        {
            beanListener = new BeanListener();
            binder.bindListener( Matchers.any(), beanListener );
            binder.requestInjection( beanListener );
        }
        beanListener.mediate( watchedKey, mediator, watcherType );
    }

    /**
     * Binds the given qualified type using a binding key based on the qualifier annotation.
     * 
     * @param qualifier The qualifier
     * @param qualifiedType The qualified type
     */
    @SuppressWarnings( "unchecked" )
    private void bindQualifiedType( final Annotation qualifier, final Class<?> qualifiedType )
    {
        final Class bindingType = getBindingType( qualifiedType );
        if ( null != bindingType )
        {
            final Annotation normalizedQualifier = normalize( qualifier, qualifiedType );
            binder.bind( Key.get( bindingType, normalizedQualifier ) ).to( qualifiedType );
            if ( BeanLocator.DEFAULT == normalizedQualifier && bindingType != qualifiedType )
            {
                binder.bind( bindingType ).to( qualifiedType );
            }
            if ( qualifiedType.isAnnotationPresent( EagerSingleton.class ) )
            {
                binder.bind( qualifiedType ).asEagerSingleton();
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
     * Determines the binding type to be used for the given qualified type.
     * 
     * @param qualifiedType The qualified type
     * @return Binding type
     */
    private Class<?> getBindingType( final Class<?> qualifiedType )
    {
        if ( qualifiedType.isInterface() )
        {
            return qualifiedType;
        }

        Class<?> clazz = qualifiedType;
        while ( true )
        {
            // @Typed annotation overrides any declared interfaces
            final Typed typed = clazz.getAnnotation( Typed.class );
            final Class<?>[] interfaces = null != typed ? typed.value() : clazz.getInterfaces();
            if ( interfaces.length == 1 )
            {
                return interfaces[0];
            }
            if ( interfaces.length > 1 )
            {
                binder.addError( qualifiedType + " has ambiguous interfaces, use @Typed to pick one" );
                return null;
            }
            // no interfaces yet, move up the type hierarchy
            final Class<?> superClazz = clazz.getSuperclass();
            if ( superClazz.getName().startsWith( "java" ) )
            {
                // no interfaces left, resort to top-most abstract type
                return Object.class != superClazz ? superClazz : clazz;
            }
            clazz = superClazz;
        }
    }

    /**
     * Normalizes the given qualifier annotation, taking the qualified type into account.
     * 
     * @param qualifier The qualifier
     * @param qualifiedType The qualified type
     * @return Normalized qualifier
     */
    private static Annotation normalize( final Annotation qualifier, final Class<?> qualifiedType )
    {
        String name = "CUSTOM";
        if ( qualifier instanceof javax.inject.Named )
        {
            name = ( (javax.inject.Named) qualifier ).value();
        }
        else if ( qualifier instanceof Named )
        {
            name = ( (Named) qualifier ).value();
        }

        if ( name.length() == 0 )
        {
            // implicit naming - use the qualified type name as a hint
            if ( !qualifiedType.getSimpleName().startsWith( "Default" ) )
            {
                return Names.named( qualifiedType.getName() );
            }
            return BeanLocator.DEFAULT;
        }
        if ( "default".equalsIgnoreCase( name ) )
        {
            return BeanLocator.DEFAULT;
        }

        return qualifier;
    }
}
