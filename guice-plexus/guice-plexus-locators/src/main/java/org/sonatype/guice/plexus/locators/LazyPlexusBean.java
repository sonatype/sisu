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

import org.sonatype.guice.bean.locators.QualifiedBean;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.DeferredProvider;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.plexus.config.PlexusBean;
import org.sonatype.guice.plexus.config.PlexusBeanDescription;

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

    private final QualifiedBean<Named, T> bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyPlexusBean( final QualifiedBean<Named, T> bean )
    {
        this.bean = bean;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getKey()
    {
        return bean.getKey().value();
    }

    public synchronized T getValue()
    {
        return bean.getValue();
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public String getDescription()
    {
        final Object source = bean.getBinding().getSource();
        return source instanceof PlexusBeanDescription ? ( (PlexusBeanDescription) source ).getDescription() : null;
    }

    public DeferredClass<T> getImplementationClass()
    {
        final DeferredTargetVisitor<T, ?> visitor = new DeferredTargetVisitor<T, Object>();
        bean.getBinding().acceptTargetVisitor( visitor );
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
    @SuppressWarnings( { "unchecked", "rawtypes" } )
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
