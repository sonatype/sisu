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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;

import org.sonatype.guice.bean.reflect.DeclaredMembers;
import org.sonatype.guice.bean.reflect.DeferredProvider;

import com.google.inject.Binding;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.UntargettedBinding;

/**
 * {@link BindingTargetVisitor} that collects the {@link Key}s of any injected dependencies.
 */
final class DependencyAnalyzer<T>
    extends DefaultBindingTargetVisitor<T, Set<Key<?>>>
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Set<Key<?>> visit( final UntargettedBinding<? extends T> binding )
    {
        return analyze( binding.getKey().getTypeLiteral() );
    }

    @Override
    public Set<Key<?>> visit( final LinkedKeyBinding<? extends T> binding )
    {
        return analyze( binding.getLinkedKey().getTypeLiteral() );
    }

    @Override
    public Set<Key<?>> visit( final ConstructorBinding<? extends T> binding )
    {
        return analyze( binding.getConstructor().getDeclaringType() );
    }

    @Override
    public Set<Key<?>> visit( final InstanceBinding<? extends T> binding )
    {
        return analyze( TypeLiteral.get( binding.getInstance().getClass() ) );
    }

    @Override
    public Set<Key<?>> visit( final ProviderInstanceBinding<? extends T> binding )
    {
        final Provider<? extends T> provider = binding.getProviderInstance();
        if ( provider instanceof DeferredProvider<?> )
        {
            try
            {
                final DeferredProvider<?> deferredProvider = (DeferredProvider<?>) provider;
                return analyze( TypeLiteral.get( deferredProvider.getImplementationClass().load() ) );
            }
            catch ( final Throwable e ) // NOPMD
            {
                // can't analyze a broken class
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Key<?>> visitOther( final Binding<? extends T> binding )
    {
        return Collections.emptySet();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Analyzes the given bean type to determine what dependencies the injector needs to provide.
     * 
     * @param type The bean type
     * @return Set of dependency keys
     */
    static Set<Key<?>> analyze( final TypeLiteral<?> type )
    {
        final DependencySet dependencies = new DependencySet();
        for ( final Member m : new DeclaredMembers( type.getRawType() ) )
        {
            if ( false == ( (AnnotatedElement) m ).isAnnotationPresent( Inject.class ) )
            {
                continue; // not an injection point
            }
            if ( m instanceof Constructor<?> )
            {
                final Constructor<?> ctor = (Constructor<?>) m;
                final List<TypeLiteral<?>> paramTypes = type.getParameterTypes( ctor );
                final Annotation[][] paramAnnotations = ctor.getParameterAnnotations();
                for ( int i = 0; i < paramAnnotations.length; i++ )
                {
                    dependencies.addDependency( paramTypes.get( i ), paramAnnotations[i] );
                }
            }
            else if ( m instanceof Method )
            {
                final Method method = (Method) m;
                final List<TypeLiteral<?>> paramTypes = type.getParameterTypes( method );
                final Annotation[][] paramAnnotations = method.getParameterAnnotations();
                for ( int i = 0; i < paramAnnotations.length; i++ )
                {
                    dependencies.addDependency( paramTypes.get( i ), paramAnnotations[i] );
                }
            }
            else
            {
                final Field f = (Field) m;
                dependencies.addDependency( type.getFieldType( f ), f.getAnnotations() );
            }
        }
        return dependencies;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Set} of qualified dependency {@link Key}s.
     */
    final static class DependencySet
        extends HashSet<Key<?>>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final long serialVersionUID = 1L;

        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        /**
         * Adds the appropriate qualified {@link Key} for the given dependency.
         * 
         * @param type The dependency type
         * @param annotations The dependency annotations
         */
        boolean addDependency( final TypeLiteral<?> type, final Annotation[] annotations )
        {
            for ( final Annotation ann : annotations )
            {
                final Class<? extends Annotation> annType = ann.annotationType();
                if ( annType.isAnnotationPresent( Qualifier.class )
                    || annType.isAnnotationPresent( BindingAnnotation.class ) )
                {
                    return add( Key.get( type, ann ) ); // qualified binding
                }
            }
            return add( Key.get( type ) ); // wildcard (unqualified) binding
        }
    }
}
