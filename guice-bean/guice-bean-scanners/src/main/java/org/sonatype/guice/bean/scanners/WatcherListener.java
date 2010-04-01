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
import org.sonatype.guice.bean.locators.Mediator;
import org.sonatype.guice.bean.reflect.Generics;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link TypeListener} and {@link Matcher} that automatically applies {@link Mediator}s to mediated watchers.
 */
final class WatcherListener
    extends AbstractMatcher<TypeLiteral<?>>
    implements TypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private final Class<Mediator> mediatorType;

    private final Key<?> qualifiedKey;

    private final Class<?> watcherType;

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    WatcherListener( final Class mediatorType )
    {
        this.mediatorType = mediatorType;
        final TypeLiteral superType = TypeLiteral.get( mediatorType ).getSupertype( Mediator.class );
        final TypeLiteral[] params = Generics.typeArguments( superType );
        if ( params.length != 3 )
        {
            throw new IllegalArgumentException( "Generic type information missing for Mediator " + mediatorType );
        }
        qualifiedKey = Key.get( params[1], params[0].getRawType() );
        watcherType = params[2].getRawType();
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
        final Provider<Mediator> mediator = encounter.getProvider( mediatorType );
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

        private final Provider<Mediator> mediator;

        private final Key qualifiedKey;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Autowatcher( final Provider<BeanLocator> locator, final Provider<Mediator> mediator, final Key qualifiedKey )
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
            locator.get().watch( qualifiedKey, mediator.get(), watcher );
        }
    }
}