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

import com.google.inject.Injector;
import com.google.inject.Key;

public final class SisuContainer
{
    private static final SisuContext MISSING_CONTEXT = new MissingContext();

    private static SisuContext context;

    public static void configure( Class<?> type, Map<String, String> properties )
    {
        injector( type ).getInstance( ParameterKeys.PROPERTIES ).putAll( properties );
    }

    public static <T> T lookup( final Key<T> key )
    {
        final BeanLocator locator = injector( key.getTypeLiteral().getRawType() ).getInstance( BeanLocator.class );
        final Iterator<? extends Entry<Annotation, T>> i = locator.locate( key, null ).iterator();
        return i.hasNext() ? i.next().getValue() : null;
    }

    public static void inject( final Object that )
    {
        injector( that.getClass() ).injectMembers( that );
    }

    static synchronized void context( final SisuContext newContext )
    {
        context = newContext;
    }

    static synchronized Injector injector( final Class<?> type )
    {
        return ( null != context ? context : MISSING_CONTEXT ).injector( type );
    }

    static final class MissingContext
        implements SisuContext
    {
        public Injector injector( Class<?> type )
        {
            throw new IllegalStateException( "No Sisu Context for thread: " + Thread.currentThread() );
        }
    }
}
