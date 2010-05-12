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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.EntryListAdapter;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.locators.NamedIterableAdapter;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

// ----------------------------------------------------------------------
// Generic Providers that can supply all sorts of bean sequences/mappings
// ----------------------------------------------------------------------

/**
 * Provides {@link Iterable} sequence of qualified beans.
 */
final class IterableProvider<K extends Annotation, V>
    implements Provider<Iterable<Entry<K, V>>>
{
    @Inject
    private BeanLocator locator;

    @Inject
    private TypeLiteral<K> qualifierType;

    @Inject
    private TypeLiteral<V> beanType;

    @SuppressWarnings( "unchecked" )
    public Iterable<Entry<K, V>> get()
    {
        return locator.locate( Key.get( beanType, (Class) qualifierType.getRawType() ) );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides {@link Iterable} sequence of hinted beans.
 */
final class IterableHintProvider<V>
    implements Provider<Iterable<Entry<String, V>>>
{
    @Inject
    private IterableProvider<Named, V> provider;

    public Iterable<Entry<String, V>> get()
    {
        return new NamedIterableAdapter<V>( provider.get() );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link List} of {@link Named} beans.
 */
final class ListProvider<T>
    implements Provider<List<T>>
{
    @Inject
    private IterableProvider<Named, T> provider;

    public List<T> get()
    {
        return new EntryListAdapter<Named, T>( provider.get() );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link Map} of qualified beans.
 */
final class MapProvider<K extends Annotation, V>
    implements Provider<Map<K, V>>
{
    @Inject
    private IterableProvider<K, V> provider;

    public Map<K, V> get()
    {
        return new EntryMapAdapter<K, V>( provider.get() );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link Map} of hinted beans.
 */
final class MapHintProvider<V>
    implements Provider<Map<String, V>>
{
    @Inject
    private IterableHintProvider<V> provider;

    public Map<String, V> get()
    {
        return new EntryMapAdapter<String, V>( provider.get() );
    }
}

// ----------------------------------------------------------------------
