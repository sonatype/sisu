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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusTypeLocator;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

@Singleton
final class AggregateTypeLocator
    implements PlexusTypeLocator
{
    @Inject
    Injector rootInjector;

    @Inject( optional = true )
    Iterable<Injector> injectorChain = Collections.emptyList();

    @SuppressWarnings( "unchecked" )
    public <T> Iterable<Entry<String, T>> locate( final TypeLiteral<T> type, final String... hints )
    {
        final Type locatorType = Types.newParameterizedType( GuiceStaticBindingLocator.class, type.getRawType() );
        final Key<GuiceStaticBindingLocator<T>> locatorKey = (Key) Key.get( locatorType );
        final String[] canonicalHints = Hints.canonicalHints( hints );

        return new Iterable<Entry<String, T>>()
        {
            public Iterator<Entry<String, T>> iterator()
            {
                return new Iterator<Entry<String, T>>()
                {
                    private final Iterator<Injector> i = injectorChain.iterator();

                    private Iterator<Entry<String, T>> e =
                        rootInjector.getInstance( locatorKey ).locate( canonicalHints ).iterator();

                    public boolean hasNext()
                    {
                        if ( e.hasNext() )
                        {
                            return true;
                        }
                        while ( i.hasNext() )
                        {
                            e = i.next().getInstance( locatorKey ).locate( canonicalHints ).iterator();
                            if ( e.hasNext() )
                            {
                                return true;
                            }
                        }
                        return false;
                    }

                    public Entry<String, T> next()
                    {
                        if ( e.hasNext() )
                        {
                            return e.next();
                        }
                        throw new NoSuchElementException();
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}