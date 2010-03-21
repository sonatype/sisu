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

import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.EntryListAdapter;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

final class QualifiedListProvider<T>
    implements Provider<List<T>>
{
    @Inject
    private BeanLocator locator;

    @Inject
    private TypeLiteral<T> beanType;

    public List<T> get()
    {
        return new EntryListAdapter<Annotation, T>( locator.locate( Key.get( beanType ) ) );
    }
}
