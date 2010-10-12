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
package org.sonatype.guice.bean.containers;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.inject.SisuContext;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public abstract class AbstractSisuContext
    implements SisuContext
{
    public void configure( Class<?> type, Map<String, String> properties )
    {
        injector( type ).getInstance( ParameterKeys.PROPERTIES ).putAll( properties );
    }

    public final <T> T lookup( final Class<T> type )
    {
        return lookup( Key.get( type ) );
    }

    public final <T> T lookup( final Class<T> type, final String name )
    {
        return lookup( type, Names.named( name ) );
    }

    public final <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
    {
        return lookup( Key.get( type, qualifier ) );
    }

    public final <T> T lookup( final Class<T> type, final Annotation qualifier )
    {
        return lookup( Key.get( type, qualifier ) );
    }

    public final void inject( final Object that )
    {
        injector( that.getClass() ).injectMembers( that );
    }

    protected abstract Injector injector( final Class<?> type );

    private final <T> T lookup( final Key<T> key )
    {
        final BeanLocator locator = injector( key.getTypeLiteral().getRawType() ).getInstance( BeanLocator.class );
        final Iterator<? extends Entry<Annotation, T>> i = locator.locate( key, null ).iterator();
        return i.hasNext() ? i.next().getValue() : null;
    }
}
