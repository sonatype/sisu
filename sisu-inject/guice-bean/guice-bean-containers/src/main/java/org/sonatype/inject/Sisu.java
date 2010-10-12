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
package org.sonatype.inject;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Properties;

public final class Sisu
{
    private static final SisuContext MISSING_CONTEXT = new MissingContext();

    private static final ThreadLocal<SisuContext> CONTEXT = new InheritableThreadLocal<SisuContext>();

    public static SisuContext context( final SisuContext newContext )
    {
        final SisuContext oldContext = CONTEXT.get();
        if ( null != newContext )
        {
            CONTEXT.set( newContext );
        }
        else
        {
            CONTEXT.remove();
        }
        return oldContext;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void configure( final Class<?> type, final Properties properties )
    {
        configure( type, (Map) properties );
    }

    public void configure( final Class<?> type, final Map<String, String> properties )
    {
        context().configure( type, properties );
    }

    public static <T> T lookup( final Class<T> type )
    {
        return context().lookup( type );
    }

    public static <T> T lookup( final Class<T> type, final String name )
    {
        return context().lookup( type, name );
    }

    public static <T> T lookup( final Class<T> type, final Annotation qualifier )
    {
        return context().lookup( type, qualifier );
    }

    public static <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
    {
        return context().lookup( type, qualifier );
    }

    public static void inject( final Object that )
    {
        context().inject( that );
    }

    private static SisuContext context()
    {
        final SisuContext context = CONTEXT.get();
        return null != context ? context : MISSING_CONTEXT;
    }

    static final class MissingContext
        implements SisuContext
    {
        private static final String MISSING_CONTEXT_MESSAGE = "No Sisu Context for thread: ";

        public void configure( final Class<?> type, final Map<String, String> properties )
        {
            throw new IllegalStateException( MISSING_CONTEXT_MESSAGE + Thread.currentThread() );
        }

        public <T> T lookup( final Class<T> type )
        {
            throw new IllegalStateException( MISSING_CONTEXT_MESSAGE + Thread.currentThread() );
        }

        public <T> T lookup( final Class<T> type, final String name )
        {
            throw new IllegalStateException( MISSING_CONTEXT_MESSAGE + Thread.currentThread() );
        }

        public <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
        {
            throw new IllegalStateException( MISSING_CONTEXT_MESSAGE + Thread.currentThread() );
        }

        public <T> T lookup( final Class<T> type, final Annotation qualifier )
        {
            throw new IllegalStateException( MISSING_CONTEXT_MESSAGE + Thread.currentThread() );
        }

        public void inject( final Object that )
        {
            throw new IllegalStateException( MISSING_CONTEXT_MESSAGE + Thread.currentThread() );
        }
    }
}
