/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
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

    public static void configure( final Class<?> type, final Map<String, String> properties )
    {
        injector( type ).getInstance( ParameterKeys.PROPERTIES ).putAll( properties );
    }

    public static <T> T lookup( final Key<T> key )
    {
        final BeanLocator locator = injector( key.getTypeLiteral().getRawType() ).getInstance( BeanLocator.class );
        final Iterator<? extends Entry<Annotation, T>> i = locator.locate( key ).iterator();
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
        public Injector injector( final Class<?> type )
        {
            throw new IllegalStateException( "No Sisu Context for thread: " + Thread.currentThread() );
        }
    }
}
