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
package org.sonatype.guice.bean.locators;

import org.sonatype.guice.bean.reflect.DeferredProvider;

import com.google.inject.Provider;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;

/**
 * {@link BindingTargetVisitor} that attempts to discover implementations behind bindings.
 */
final class ImplementationVisitor
    implements BindingTargetVisitor<Object, Class<?>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final ImplementationVisitor THIS = new ImplementationVisitor();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> visit( final UntargettedBinding<?> binding )
    {
        return binding.getKey().getTypeLiteral().getRawType();
    }

    public Class<?> visit( final LinkedKeyBinding<?> binding )
    {
        // this assumes only one level of indirection: api-->impl
        return binding.getLinkedKey().getTypeLiteral().getRawType();
    }

    public Class<?> visit( final ConstructorBinding<?> binding )
    {
        return binding.getConstructor().getDeclaringType().getRawType();
    }

    public Class<?> visit( final InstanceBinding<?> binding )
    {
        return binding.getInstance().getClass();
    }

    public Class<?> visit( final ProviderInstanceBinding<?> binding )
    {
        final Provider<?> provider = binding.getProviderInstance();
        if ( provider instanceof DeferredProvider<?> )
        {
            try
            {
                // deferred providers let us peek at the underlying implementation type
                return ( (DeferredProvider<?>) provider ).getImplementationClass().load();
            }
            catch ( final TypeNotPresentException e ) // NOPMD
            {
                // fall-through
            }
        }
        return null;
    }

    public Class<?> visit( final ExposedBinding<?> binding )
    {
        return binding.getPrivateElements().getInjector().getBinding( binding.getKey() ).acceptTargetVisitor( this );
    }

    // ----------------------------------------------------------------------
    // These bindings can't give us the implementation without side-effects
    // ----------------------------------------------------------------------

    public Class<?> visit( final ProviderBinding<?> binding )
    {
        return null;
    }

    public Class<?> visit( final ProviderKeyBinding<?> binding )
    {
        return null;
    }

    public Class<?> visit( final ConvertedConstantBinding<?> binding )
    {
        return null;
    }
}
