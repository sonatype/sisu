/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.locators;

import java.util.Map.Entry;

import javax.inject.Provider;

import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.DeferredProvider;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBean;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;

/**
 * {@link Entry} representing a lazy @{@link Named} Plexus bean; the bean is only retrieved when the value is requested.
 */
final class LazyPlexusBean<T>
    implements PlexusBean<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binding<T> binding;

    private T bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyPlexusBean( final Binding<T> binding )
    {
        this.binding = binding;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getKey()
    {
        final Named hint = (Named) binding.getKey().getAnnotation();
        return null != hint ? hint.value() : Hints.DEFAULT_HINT;
    }

    public synchronized T getValue()
    {
        if ( null == bean )
        {
            bean = binding.getProvider().get();
        }
        return bean;
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public String getDescription()
    {
        final Object source = binding.getSource();
        return source instanceof String ? source.toString() : null;
    }

    public DeferredClass<T> getImplementationClass()
    {
        final DeferredTargetVisitor<T, ?> visitor = new DeferredTargetVisitor<T, Object>();
        binding.acceptTargetVisitor( visitor );
        return visitor.implementationClass;
    }

    @Override
    public String toString()
    {
        return getKey() + "=" + getValue();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BindingTargetVisitor} that deduces the implementation class for a given {@link Binding}.
     */
    @SuppressWarnings( "unchecked" )
    static final class DeferredTargetVisitor<T, V>
        extends DefaultBindingTargetVisitor<T, V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        DeferredClass<T> implementationClass;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public V visit( final ProviderInstanceBinding<? extends T> providerInstanceBinding )
        {
            final Provider provider = providerInstanceBinding.getProviderInstance();
            if ( provider instanceof DeferredProvider )
            {
                implementationClass = ( (DeferredProvider) provider ).getImplementationClass();
            }
            return null;
        }

        @Override
        public V visit( final LinkedKeyBinding<? extends T> linkedKeyBinding )
        {
            final Key key = linkedKeyBinding.getLinkedKey();
            implementationClass = new LoadedClass( key.getTypeLiteral().getRawType() );
            return null;
        }

        @Override
        public V visit( final ConstructorBinding<? extends T> constructorBinding )
        {
            implementationClass = new LoadedClass( constructorBinding.getConstructor().getDeclaringType().getRawType() );
            return null;
        }

        @Override
        public V visit( final InstanceBinding<? extends T> instanceBinding )
        {
            implementationClass = new LoadedClass( instanceBinding.getInstance().getClass() );
            return null;
        }
    }
}
