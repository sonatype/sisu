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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.locators.NamedIterableAdapter;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

final class QualifiedHintProvider<V>
    implements Provider<Map<String, V>>
{
    @Inject
    private BeanLocator locator;

    @Inject
    private TypeLiteral<V> valueType;

    @SuppressWarnings( "unchecked" )
    public Map<String, V> get()
    {
        final Iterable beans = locator.locate( Key.get( valueType, Named.class ) );
        return new EntryMapAdapter( new NamedIterableAdapter( beans ) );
    }
}