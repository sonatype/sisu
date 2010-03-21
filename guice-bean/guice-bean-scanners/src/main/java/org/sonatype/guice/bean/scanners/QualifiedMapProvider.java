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
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.EntryMapAdapter;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

final class QualifiedMapProvider<K extends Annotation, V>
    implements Provider<Map<K, V>>
{
    @Inject
    private BeanLocator locator;

    @Inject
    private TypeLiteral<K> qualifierType;

    @Inject
    private TypeLiteral<V> beanType;

    @SuppressWarnings( "unchecked" )
    public Map<K, V> get()
    {
        final Key qualifierKey = Key.get( beanType, (Class<Annotation>) qualifierType.getRawType() );
        return new EntryMapAdapter( locator.locate( qualifierKey ) );
    }
}