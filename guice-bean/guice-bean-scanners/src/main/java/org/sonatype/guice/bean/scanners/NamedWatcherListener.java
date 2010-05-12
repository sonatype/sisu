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

import javax.inject.Provider;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.reflect.Generics;
import org.sonatype.inject.Mediator;
import org.sonatype.inject.NamedMediator;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.name.Named;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link TypeListener} and {@link Matcher} that automatically applies {@link Mediator}s to mediated watchers.
 */
final class NamedWatcherListener
    extends AbstractMatcher<TypeLiteral<?>>
    implements TypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private final Class<NamedMediator> mediatorType;

    private final Key<?> qualifiedKey;

    private final Class<?> watcherType;

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    NamedWatcherListener( final Class mediatorType )
    {
        this.mediatorType = mediatorType;
        final TypeLiteral superType = TypeLiteral.get( mediatorType ).getSupertype( NamedMediator.class );
        final TypeLiteral[] params = Generics.typeArguments( superType );
        if ( params.length != 2 )
        {
            throw new IllegalArgumentException( mediatorType + " has wrong number of type arguments" );
        }
        qualifiedKey = Key.get( params[0].getRawType(), Named.class );
        watcherType = params[1].getRawType();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean matches( final TypeLiteral<?> type )
    {
        return watcherType.isAssignableFrom( type.getRawType() );
    }

    public <I> void hear( final TypeLiteral<I> watchedType, final TypeEncounter<I> encounter )
    {
        @SuppressWarnings( "unchecked" )
        final Provider<NamedMediator> mediator = encounter.getProvider( mediatorType );
        final Provider<BeanLocator> locator = encounter.getProvider( BeanLocator.class );
        encounter.register( new Autowatcher<I>( locator, mediator, qualifiedKey ) );
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    /**
     * {@link InjectionListener} that applies {@link Mediator}s to watcher instances.
     */
    @SuppressWarnings( "unchecked" )
    private static final class Autowatcher<I>
        implements InjectionListener<I>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Provider<BeanLocator> locator;

        private final Provider<NamedMediator> mediator;

        private final Key qualifiedKey;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Autowatcher( final Provider<BeanLocator> locator, final Provider<NamedMediator> mediator, final Key qualifiedKey )
        {
            this.locator = locator;
            this.mediator = mediator;

            this.qualifiedKey = qualifiedKey;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void afterInjection( final I watcher )
        {
            final NamedMediator namedMediator = mediator.get();
            locator.get().watch( qualifiedKey, new Mediator<Named, Object, Object>()
            {
                public void add( final Named name, final Provider bean, final Object w )
                    throws Exception
                {
                    namedMediator.add( name.value(), bean, w );
                }

                public void remove( final Named name, final Provider bean, final Object w )
                    throws Exception
                {
                    namedMediator.remove( name.value(), bean, w );
                }
            }, watcher );
        }
    }
}