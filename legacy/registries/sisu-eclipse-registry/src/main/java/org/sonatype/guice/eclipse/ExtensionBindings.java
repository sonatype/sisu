/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.eclipse;

import java.security.ProviderException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;
import org.eclipse.sisu.locators.MutableBeanLocator;
import org.eclipse.sisu.locators.spi.BindingPublisher;
import org.eclipse.sisu.locators.spi.BindingSubscriber;
import org.eclipse.sisu.EagerSingleton;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderInstanceBinding;

/**
 * {@link BindingPublisher} that queries the Eclipse {@link IExtensionRegistry}.
 */
@Named
@EagerSingleton
final class ExtensionBindings
    implements BindingPublisher
{
    private final IExtensionRegistry registry;

    @Inject
    ExtensionBindings( final MutableBeanLocator locator )
    {
        registry = RegistryFactory.getRegistry();
        locator.add( this, -1 );
    }

    public <T> void subscribe( final BindingSubscriber<T> subscriber )
    {
        final TypeLiteral<T> type = subscriber.type();
        final Class<?> clazz = type.getRawType();
        final String pointId = clazz.getPackage().getName(); // FIXME
        for ( final IConfigurationElement config : registry.getConfigurationElementsFor( pointId ) )
        {
            try
            {
                final String name = config.getAttribute( "class" );
                if ( null != name )
                {
                    if ( type.getRawType().isAssignableFrom( loadExtensionClass( config, name ) ) )
                    {
                        subscriber.add( new ExtensionBinding<T>( type, config ), 0 );
                    }
                }
            }
            catch ( final Exception e ) // NOPMD
            {
                // check next element
            }
            catch ( final LinkageError e ) // NOPMD
            {
                // check next element
            }
        }
    }

    public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
    {
        for ( final Binding<T> binding : subscriber.bindings() )
        {
            if ( binding instanceof ExtensionBinding<?> )
            {
                subscriber.remove( binding );
            }
        }
    }

    private static final class ExtensionBinding<T>
        implements ProviderInstanceBinding<T>
    {
        private final TypeLiteral<T> type;

        final IConfigurationElement config;

        ExtensionBinding( final TypeLiteral<T> type, final IConfigurationElement config )
        {
            this.type = type;
            this.config = config;
        }

        public Key<T> getKey()
        {
            return Key.get( type );
        }

        @SuppressWarnings( "unchecked" )
        public Provider<T> getProvider()
        {
            return new Provider<T>()
            {
                public T get()
                {
                    try
                    {
                        return (T) config.createExecutableExtension( "class" );
                    }
                    catch ( final CoreException e )
                    {
                        throw new ProviderException( e );
                    }
                }
            };
        }

        public Object getSource()
        {
            return config;
        }

        public Set<Dependency<?>> getDependencies()
        {
            return Collections.emptySet();
        }

        public Set<InjectionPoint> getInjectionPoints()
        {
            return Collections.emptySet();
        }

        public Provider<T> getProviderInstance()
        {
            return getProvider();
        }

        public void applyTo( final Binder binder )
        {
            binder.bind( getKey() ).toProvider( getProvider() );
        }

        public <S> S acceptVisitor( final ElementVisitor<S> visitor )
        {
            return visitor.visit( this );
        }

        public <V> V acceptScopingVisitor( final BindingScopingVisitor<V> visitor )
        {
            return visitor.visitNoScoping();
        }

        public <V> V acceptTargetVisitor( final BindingTargetVisitor<? super T, V> visitor )
        {
            return visitor.visit( this );
        }
    }

    static Class<?> loadExtensionClass( final IConfigurationElement config, final String clazzName )
        throws ClassNotFoundException
    {
        final Bundle bundle = ContributorFactoryOSGi.resolve( config.getContributor() );

        final String value = clazzName;
        final int n = value.indexOf( ':' );

        return bundle.loadClass( n < 0 ? value : value.substring( 0, n ) );
    }
}
